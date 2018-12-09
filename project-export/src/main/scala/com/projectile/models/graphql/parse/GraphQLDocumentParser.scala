package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportEnum, ExportModel}
import com.projectile.models.input.InputType
import com.projectile.util.Logging
import sangria.ast._
import sangria.schema.Schema

import scala.annotation.tailrec

object GraphQLDocumentParser extends Logging {
  def parse(schema: Schema[_, _], doc: Document) = {
    val fragments = doc.fragments.values
    val inputs = doc.definitions.collect { case x: InputObjectTypeDefinition => x }
    val mutations = doc.operations.filter(_._2.operationType == OperationType.Mutation).values
    val queries = doc.operations.filter(_._2.operationType == OperationType.Query).values

    val total = Seq(
      fragments.map(f => parseFragment(schema, doc, f)),
      inputs.map(i => parseInput(schema, doc, i)),
      mutations.map(m => parseMutation(schema, doc, m)),
      queries.map(q => parseQuery(schema, doc, q))
    ).flatten

    val ret = total.map(Right.apply)

    @tailrec
    def addReferences(s: Seq[Either[ExportEnum, ExportModel]]): Seq[Either[ExportEnum, ExportModel]] = {
      val current = s.map {
        case Left(enum) => enum.key
        case Right(model) => model.key
      }.toSet

      val extras = getExtras(schema, current, s)

      if (extras.isEmpty) {
        log.info(s" ::: Completed with keys [${current.toSeq.sorted.mkString(", ")}]")
        s
      } else {
        val keys = extras.map {
          case Left(x) => x.key
          case Right(x) => x.key
        }
        log.info(s" ::: Loading extras [${keys.mkString(", ")}]")
        addReferences(s ++ extras)
      }
    }

    addReferences(ret)
  }

  private[this] def parseFragment(schema: Schema[_, _], doc: Document, f: FragmentDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(s"fragment:${f.name}", schema, doc, schema.allTypes(f.typeCondition.name), f.selections)
    GraphQLDocumentHelper.modelFor(f.name, InputType.Model.GraphQLFragment, Nil, fields)
  }

  private[this] def parseInput(schema: Schema[_, _], doc: Document, i: InputObjectTypeDefinition) = {
    val fields = i.fields.map(f => GraphQLFieldParser.getField(i.name, schema, doc, f.name, f.valueType, f.defaultValue))
    GraphQLDocumentHelper.modelFor(i.name, InputType.Model.GraphQLInput, Nil, fields)
  }

  private[this] def parseMutation(schema: Schema[_, _], doc: Document, o: OperationDefinition) = {
    parseOperation(schema, doc, InputType.Model.GraphQLMutation, o)
  }

  private[this] def parseQuery(schema: Schema[_, _], doc: Document, o: OperationDefinition) = {
    parseOperation(schema, doc, InputType.Model.GraphQLQuery, o)
  }

  private[this] def parseOperation(schema: Schema[_, _], doc: Document, it: InputType.Model, o: OperationDefinition) = {
    val key = o.name.getOrElse(throw new IllegalStateException("All operations must be named"))
    val typ = o.operationType match {
      case OperationType.Query => schema.query
      case OperationType.Mutation => schema.mutation.get
      case _ => throw new IllegalStateException(s"Unsupported operation [${o.operationType}]")
    }
    val fields = GraphQLSelectionParser.fieldsForSelections(s"$it:$key", schema, doc, typ, o.selections)
    val vars = GraphQLDocumentHelper.parseVariables(schema, doc, o.variables)
    GraphQLDocumentHelper.modelFor(key, it, vars, fields)
  }

  private[this] def getExtras(schema: Schema[_, _], current: Set[String], s: Seq[Either[ExportEnum, ExportModel]]) = {
    def forType(t: FieldType, isInput: Boolean): Seq[Either[ExportEnum, ExportModel]] = t match {
      case FieldType.EnumType(key) if !current.apply(key) => GraphQLDocumentHelper.enumFromSchema(schema, key)
      case FieldType.StructType(key) if !current.apply(key) => GraphQLDocumentHelper.modelFromSchema(schema, key, isInput)
      case FieldType.ListType(typ) => forType(typ, isInput)
      case FieldType.SetType(typ) => forType(typ, isInput)
      case FieldType.MapType(k, v) => forType(k, isInput) ++ forType(v, isInput)
      case FieldType.ObjectType(_, fields) => fields.flatMap(f => forType(f.v, isInput))
      case _ => Nil
    }

    s.collect {
      case Right(m) if m.inputType == InputType.Model.GraphQLInput => (m.arguments ++ m.fields).map(f => f.t -> true)
      case Right(m) => (m.arguments.map(f => f.t -> true) ++ m.fields.map(f => f.t -> false))
    }.flatten.distinct.flatMap(x => forType(x._1, x._2))
  }
}
