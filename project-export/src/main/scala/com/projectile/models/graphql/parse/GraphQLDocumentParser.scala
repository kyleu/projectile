package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportEnum, ExportModel}
import com.projectile.models.input.InputType
import com.projectile.util.Logging
import sangria.ast._
import sangria.schema.Schema

import scala.annotation.tailrec

object GraphQLDocumentParser extends Logging {
  def parse(pkg: Seq[String], schema: Schema[_, _], doc: Document) = {
    val fragments = doc.fragments.values
    val inputs = doc.definitions.collect { case x: InputObjectTypeDefinition => x }
    val mutations = doc.operations.filter(_._2.operationType == OperationType.Mutation).values
    val queries = doc.operations.filter(_._2.operationType == OperationType.Query).values

    val total = Seq(
      fragments.map(f => parseFragment(pkg, schema, doc, f)),
      inputs.map(i => parseInput(pkg, schema, doc, i)),
      mutations.map(m => parseMutation(pkg, schema, doc, m)),
      queries.map(q => parseQuery(pkg, schema, doc, q))
    ).flatten

    val ret = total.map(Right.apply)

    @tailrec
    def addReferences(s: Seq[Either[ExportEnum, ExportModel]]): Seq[Either[ExportEnum, ExportModel]] = {
      val current = s.map {
        case Left(enum) => enum.key
        case Right(model) => model.key
      }.toSet

      val extras = getExtras(pkg, schema, current, s)
      if (extras.isEmpty) { s } else { addReferences(s ++ extras) }
    }

    addReferences(ret)
  }

  private[this] def parseFragment(pkg: Seq[String], schema: Schema[_, _], doc: Document, f: FragmentDefinition) = {
    val result = GraphQLSelectionParser.fieldsForSelections(s"fragment:${f.name}", schema, doc, schema.allTypes(f.typeCondition.name), f.selections)
    val fields = result.right.getOrElse(throw new IllegalStateException("Cannot currently support fragments with a single spread"))
    GraphQLDocumentHelper.modelFor(pkg, f.name, InputType.Model.GraphQLFragment, Nil, fields, Some(f.renderPretty))
  }

  private[this] def parseInput(pkg: Seq[String], schema: Schema[_, _], doc: Document, i: InputObjectTypeDefinition) = {
    val fields = i.fields.map(f => GraphQLFieldParser.getField(i.name, schema, doc, f.name, f.valueType, f.defaultValue))
    GraphQLDocumentHelper.modelFor(pkg, i.name, InputType.Model.GraphQLInput, Nil, fields, Some(i.renderPretty))
  }

  private[this] def parseMutation(pkg: Seq[String], schema: Schema[_, _], doc: Document, o: OperationDefinition) = {
    parseOperation(pkg, schema, doc, InputType.Model.GraphQLMutation, o)
  }

  private[this] def parseQuery(pkg: Seq[String], schema: Schema[_, _], doc: Document, o: OperationDefinition) = {
    parseOperation(pkg, schema, doc, InputType.Model.GraphQLQuery, o)
  }

  private[this] def parseOperation(pkg: Seq[String], schema: Schema[_, _], doc: Document, it: InputType.Model, o: OperationDefinition) = {
    val key = o.name.getOrElse(throw new IllegalStateException("All operations must be named"))
    val typ = o.operationType match {
      case OperationType.Query => schema.query
      case OperationType.Mutation => schema.mutation.get
      case _ => throw new IllegalStateException(s"Unsupported operation [${o.operationType}]")
    }
    val result = GraphQLSelectionParser.fieldsForSelections(s"$it:$key", schema, doc, typ, o.selections)
    val fields = result.right.getOrElse(throw new IllegalStateException("Cannot currently support fragments with a single spread"))
    val vars = GraphQLDocumentHelper.parseVariables(schema, doc, o.variables)
    GraphQLDocumentHelper.modelFor(pkg, key, it, vars, fields, Some(o.renderPretty))
  }

  private[this] def getExtras(pkg: Seq[String], schema: Schema[_, _], current: Set[String], s: Seq[Either[ExportEnum, ExportModel]]) = {
    def forType(t: FieldType, isInput: Boolean): Seq[Either[ExportEnum, ExportModel]] = t match {
      case FieldType.EnumType(key) if !current.apply(key) => GraphQLDocumentHelper.enumFromSchema(pkg, schema, key)
      case FieldType.StructType(key) if !current.apply(key) => GraphQLDocumentHelper.modelFromSchema(pkg, schema, key, isInput)
      case FieldType.ListType(typ) => forType(typ, isInput)
      case FieldType.SetType(typ) => forType(typ, isInput)
      case FieldType.MapType(k, v) => forType(k, isInput) ++ forType(v, isInput)
      case FieldType.ObjectType(_, fields) => fields.flatMap(f => forType(f.v, isInput))
      case _ => Nil
    }

    s.collect {
      case Right(m) if m.inputType == InputType.Model.GraphQLInput => (m.arguments ++ m.fields).map(f => f.t -> true)
      case Right(m) => m.arguments.map(f => f.t -> true) ++ m.fields.map(f => f.t -> false)
    }.flatten.distinct.flatMap(x => forType(x._1, x._2))
  }
}
