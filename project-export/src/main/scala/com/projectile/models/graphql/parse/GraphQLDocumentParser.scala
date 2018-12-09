package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportEnum, ExportField, ExportModel}
import com.projectile.models.input.InputType
import com.projectile.models.output.ExportHelper
import com.projectile.util.Logging
import sangria.ast._
import sangria.schema.{EnumType, InputObjectType, Schema}

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

    val argTypes = {
      val current = total.map(_.key).toSet
      total.flatMap(m => (m.arguments ++ m.fields).map(_.t)).toSeq.distinct.collect {
        case FieldType.EnumType(key) if !current.apply(key) => enumFromSchema(schema, key)
        case FieldType.StructType(key) if !current.apply(key) => modelFromSchema(schema, key)
      }.flatten
    }

    log.info(s" ::: $argTypes")

    val ret = total.map(Right.apply).toSeq ++ argTypes

    @tailrec
    def addReferences(s: Seq[Either[ExportEnum, ExportModel]]): Seq[Either[ExportEnum, ExportModel]] = {
      val current = s.map {
        case Left(enum) => enum.key
        case Right(model) => model.key
      }.toSet

      val extras = s.flatMap {
        case Left(_) => Nil
        case Right(model) => model.fields.map(_.t)
      }.distinct.collect {
        case FieldType.EnumType(key) if !current.apply(key) => enumFromSchema(schema, key)
        case FieldType.StructType(key) if !current.apply(key) => modelFromSchema(schema, key)
      }.flatten

      val keys = (s ++ extras).map {
        case Left(x) => x.key
        case Right(x) => x.key
      }

      if (extras.isEmpty) {
        log.info(s" ::: Completed with keys [${keys.mkString(", ")}]")
        s
      } else {
        log.info(s" ::: Loading extras [${keys.mkString(", ")}]")
        addReferences(s ++ extras)
      }
    }

    addReferences(ret)
  }

  private[this] def parseFragment(schema: Schema[_, _], doc: Document, key: String, f: FragmentDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, doc, f.selections)
    modelFor(key, InputType.Model.GraphQLFragment, Nil, fields)
  }

  private[this] def parseInput(schema: Schema[_, _], doc: Document, i: InputObjectTypeDefinition) = {
    val fields = i.fields.zipWithIndex.map(f => GraphQLFieldParser.getField(i.name, schema, doc, f._1.name, f._1.valueType, f._2, f._1.defaultValue))
    modelFor(i.name, InputType.Model.GraphQLInput, Nil, fields)
  }

  private[this] def parseMutation(schema: Schema[_, _], doc: Document, key: Option[String], o: OperationDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, doc, o.selections)
    modelFor(o.name.getOrElse("DefaultMutation"), InputType.Model.GraphQLMutation, parseVariables(schema, doc, o.variables), fields)
  }

  private[this] def parseQuery(schema: Schema[_, _], doc: Document, key: Option[String], o: OperationDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, doc, o.selections)
    modelFor(o.name.getOrElse("DefaultQuery"), InputType.Model.GraphQLQuery, parseVariables(schema, doc, o.variables), fields)
  }

  private[this] def modelFor(name: String, it: InputType.Model, arguments: Seq[ExportField], fields: Seq[ExportField]) = {
    val cn = ExportHelper.toClassName(name)
    val title = ExportHelper.toDefaultTitle(cn)

    ExportModel(
      inputType = it,
      key = name,
      pkg = it match {
        case InputType.Model.GraphQLFragment => List("graphql", "fragment")
        case InputType.Model.GraphQLInput => List("graphql", "input")
        case InputType.Model.GraphQLMutation => List("graphql", "mutation")
        case InputType.Model.GraphQLQuery => List("graphql", "query")
        case _ => throw new IllegalStateException()
      },
      propertyName = ExportHelper.toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      arguments = arguments.toList,
      fields = fields.toList
    )
  }

  private[this] def modelFromSchema(schema: Schema[_, _], key: String) = {
    log.info(s" ::: Loading model [$key] from schema")
    schema.allTypes.get(key) match {
      case Some(t) => t match {
        case i: InputObjectType[_] =>
          val fields = i.fields.zipWithIndex.map(f => GraphQLFieldParser.getInputField(i.name, schema, f._1.name, f._1.fieldType, f._2))
          log.info(s" ::: Loading model [$key]")
          Seq(Right(modelFor(i.name, InputType.Model.GraphQLInput, Nil, fields)))
        case _ => throw new IllegalStateException(s"Invalid model type [$t]")
      }
      case _ =>
        val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
        throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
    }
  }

  private[this] def enumFromSchema(schema: Schema[_, _], key: String) = {
    log.info(s" ::: Loading enum [$key] from schema")
    schema.allTypes.get(key) match {
      case Some(t) => t match {
        case EnumType(name, _, values, _, _) => Seq(Left(ExportEnum(
          inputType = InputType.Enum.GraphQLEnum,
          pkg = List("graphql", "enums"),
          key = name,
          className = ExportHelper.toClassName(name),
          values = values.map(_.name)
        )))
        case _ => throw new IllegalStateException(s"Invalid enum type [$t]")
      }
      case _ =>
        val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
        throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
    }
  }

  private[this] def parseVariables(schema: Schema[_, _], doc: Document, variables: Seq[VariableDefinition]) = variables.zipWithIndex.map { v =>
    GraphQLFieldParser.getField(
      ctx = "",
      schema = schema,
      doc = doc,
      name = v._1.name,
      t = v._1.tpe,
      idx = v._2,
      defaultValue = v._1.defaultValue
    )
  }
}
