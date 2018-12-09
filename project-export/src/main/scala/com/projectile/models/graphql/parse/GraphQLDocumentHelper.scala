package com.projectile.models.graphql.parse

import com.projectile.models.export.{ExportEnum, ExportField, ExportModel}
import com.projectile.models.input.InputType
import com.projectile.models.output.ExportHelper
import sangria.ast.{Document, VariableDefinition}
import sangria.schema.{EnumType, InputObjectType, Schema}

object GraphQLDocumentHelper {
  def modelFor(name: String, it: InputType.Model, arguments: Seq[ExportField], fields: Seq[ExportField]) = {
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

  def modelFromSchema(schema: Schema[_, _], key: String) = {
    schema.allTypes.get(key) match {
      case Some(t) => t match {
        case i: InputObjectType[_] =>
          val fields = i.fields.zipWithIndex.map(f => GraphQLFieldParser.getInputField(i.name, schema, f._1.name, f._1.fieldType, f._2))
          Seq(Right(modelFor(i.name, InputType.Model.GraphQLInput, Nil, fields)))
        case _ => throw new IllegalStateException(s"Invalid model type [$t]")
      }
      case _ =>
        val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
        throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
    }
  }

  def enumFromSchema(schema: Schema[_, _], key: String) = {
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

  def parseVariables(schema: Schema[_, _], doc: Document, variables: Seq[VariableDefinition]) = variables.zipWithIndex.map { v =>
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
