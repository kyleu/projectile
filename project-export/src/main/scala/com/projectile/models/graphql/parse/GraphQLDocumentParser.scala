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

      val extras = s.flatMap {
        case Left(e) => Nil
        case Right(m) => (m.arguments ++ m.fields).map(_.t)
      }.distinct.collect {
        case FieldType.EnumType(key) if !current.apply(key) => GraphQLDocumentHelper.enumFromSchema(schema, key)
        case FieldType.StructType(key) if !current.apply(key) => GraphQLDocumentHelper.modelFromSchema(schema, key)
      }.flatten

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
}
