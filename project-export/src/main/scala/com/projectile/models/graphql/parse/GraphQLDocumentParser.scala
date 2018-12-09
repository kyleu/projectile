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
    val fragments = doc.fragments.map(f => parseFragment(schema, doc, f._1, f._2))
    val inputs = doc.definitions.collect {
      case x: InputObjectTypeDefinition => parseInput(schema, doc, x)
    }
    val mutations = doc.operations.filter(_._2.operationType == OperationType.Mutation).map(q => parseMutation(schema, doc, q._1, q._2))
    val queries = doc.operations.filter(_._2.operationType == OperationType.Query).map(q => parseQuery(schema, doc, q._1, q._2))

    val total = fragments ++ inputs ++ mutations ++ queries

    val ret = total.map(Right.apply).toSeq

    @tailrec
    def addReferences(s: Seq[Either[ExportEnum, ExportModel]]): Seq[Either[ExportEnum, ExportModel]] = {
      val current = s.map {
        case Left(enum) => enum.key
        case Right(model) => model.key
      }.toSet

      val extras = s.flatMap {
        case Left(_) => Nil
        case Right(m) => (m.arguments ++ m.fields).map(_.t)
      }.distinct.collect {
        case FieldType.EnumType(key) if !current.apply(key) => GraphQLDocumentHelper.enumFromSchema(schema, key)
        case FieldType.StructType(key) if !current.apply(key) => GraphQLDocumentHelper.modelFromSchema(schema, key)
      }.flatten

      if (extras.isEmpty) {
        val keys = s.map {
          case Left(x) => x.key
          case Right(x) => x.key
        }
        log.info(s" ::: Completed with keys [${keys.mkString(", ")}]")
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

  private[this] def parseFragment(schema: Schema[_, _], doc: Document, key: String, f: FragmentDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, doc, s"fragment:$key", f.typeCondition, f.selections)
    GraphQLDocumentHelper.modelFor(key, InputType.Model.GraphQLFragment, Nil, fields)
  }

  private[this] def parseInput(schema: Schema[_, _], doc: Document, i: InputObjectTypeDefinition) = {
    val fields = i.fields.map(f => GraphQLFieldParser.getField(i.name, schema, doc, f.name, f.valueType, f.defaultValue))
    GraphQLDocumentHelper.modelFor(i.name, InputType.Model.GraphQLInput, Nil, fields)
  }

  private[this] def parseMutation(schema: Schema[_, _], doc: Document, key: Option[String], o: OperationDefinition) = {
    parseOperation(schema, doc, InputType.Model.GraphQLMutation, key.getOrElse("DefaultMutation"), o)
  }

  private[this] def parseQuery(schema: Schema[_, _], doc: Document, key: Option[String], o: OperationDefinition) = {
    parseOperation(schema, doc, InputType.Model.GraphQLQuery, key.getOrElse("DefaultQuery"), o)
  }

  private[this] def parseOperation(schema: Schema[_, _], doc: Document, it: InputType.Model, key: String, o: OperationDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, doc, s"$it:$key", NamedType(key), o.selections)
    val vars = GraphQLDocumentHelper.parseVariables(schema, doc, o.variables)
    GraphQLDocumentHelper.modelFor(o.name.getOrElse("DefaultOperation"), it, vars, fields)
  }
}
