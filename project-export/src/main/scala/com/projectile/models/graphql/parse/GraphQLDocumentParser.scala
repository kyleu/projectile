package com.projectile.models.graphql.parse

import com.projectile.models.export.typ.FieldType
import com.projectile.models.export.{ExportEnum, ExportField, ExportModel}
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.EnumMember
import com.projectile.models.project.member.ModelMember.InputType
import sangria.ast._
import sangria.schema.{EnumType, Schema}

import scala.annotation.tailrec

object GraphQLDocumentParser {
  def parse(schema: Schema[_, _], doc: Document) = {
    val fragments = doc.fragments.map(f => parseFragment(schema, f._1, f._2))
    val inputs = doc.definitions.collect {
      case x: InputObjectTypeDefinition => parseInput(schema, doc, x)
    }
    val mutations = doc.operations.filter(_._2.operationType == OperationType.Mutation).map(q => parseMutation(schema, q._1, q._2))
    val queries = doc.operations.filter(_._2.operationType == OperationType.Query).map(q => parseQuery(schema, q._1, q._2))

    val ret = (fragments ++ inputs ++ mutations ++ queries).map(Right.apply).toSeq

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
        case FieldType.EnumType(key) if !current.apply(key) => Left(enumFromSchema(schema, key))
        case FieldType.StructType(key) if !current.apply(key) => Right(modelFromSchema(schema, key))
      }
      if (extras.isEmpty) {
        s ++ extras
      } else {
        addReferences(s ++ extras)
      }
    }

    addReferences(ret)
  }

  private[this] def parseFragment(schema: Schema[_, _], key: String, f: FragmentDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, f.selections)
    modelFor(key, InputType.GraphQLFragment, fields)
  }

  private[this] def parseInput(schema: Schema[_, _], doc: Document, i: InputObjectTypeDefinition) = {
    val fields = i.fields.zipWithIndex.map(f => GraphQLFieldParser.getField(i.name, schema, doc, f._1.name, f._1.valueType, f._2, f._1.defaultValue))
    modelFor(i.name, InputType.GraphQLInput, fields)
  }

  private[this] def parseMutation(schema: Schema[_, _], key: Option[String], o: OperationDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, o.selections)
    modelFor(o.name.getOrElse("DefaultMutation"), InputType.GraphQLMutation, fields)
  }

  private[this] def parseQuery(schema: Schema[_, _], key: Option[String], o: OperationDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, o.selections)
    modelFor(o.name.getOrElse("DefaultQuery"), InputType.GraphQLQuery, fields)
  }

  private[this] def modelFor(name: String, it: InputType, fields: Seq[ExportField]) = {
    val cn = ExportHelper.toClassName(name)
    val title = ExportHelper.toDefaultTitle(cn)

    ExportModel(
      inputType = it,
      key = name,
      pkg = it match {
        case InputType.GraphQLFragment => List("graphql", "fragment")
        case InputType.GraphQLInput => List("graphql", "input")
        case InputType.GraphQLMutation => List("graphql", "mutation")
        case InputType.GraphQLQuery => List("graphql", "query")
        case _ => throw new IllegalStateException()
      },
      propertyName = ExportHelper.toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      fields = fields.toList
    )
  }

  private[this] def modelFromSchema(schema: Schema[_, _], key: String): ExportModel = schema.allTypes.get(key) match {
    case Some(t) => t match {
      case _ => throw new IllegalStateException(s"Invalid model type [$t]")
    }
    case _ =>
      val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
      throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
  }

  private[this] def enumFromSchema(schema: Schema[_, _], key: String) = schema.allTypes.get(key) match {
    case Some(t) => t match {
      case EnumType(name, _, values, _, _) => ExportEnum(
        inputType = EnumMember.InputType.GraphQLEnum,
        pkg = List("graphql", "enums"),
        key = name,
        className = ExportHelper.toClassName(name),
        values = values.map(_.name)
      )
      case _ => throw new IllegalStateException(s"Invalid enum type [$t]")
    }
    case _ =>
      val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
      throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
  }
}
