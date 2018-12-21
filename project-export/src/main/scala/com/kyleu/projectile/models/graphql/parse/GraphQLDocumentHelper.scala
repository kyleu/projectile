package com.kyleu.projectile.models.graphql.parse

import com.kyleu.projectile.models.export.{ExportEnum, ExportField, ExportModel}
import com.kyleu.projectile.models.input.InputType
import com.kyleu.projectile.models.output.ExportHelper.{toClassName, toDefaultTitle, toIdentifier}
import sangria.ast.{Document, VariableDefinition}
import sangria.schema.{EnumType, InputObjectType, ObjectType, Schema}

object GraphQLDocumentHelper {
  def forGraphQLName(s: String) = s

  def modelFor(pkg: Seq[String], name: String, it: InputType.Model, arguments: Seq[ExportField], fields: Seq[ExportField], source: Option[String]) = {
    val cn = forGraphQLName(toClassName(name))
    val title = toDefaultTitle(cn)

    ExportModel(
      inputType = it,
      key = name,
      pkg = pkg.toList ++ (it match {
        case InputType.Model.GraphQLFragment => List("graphql", "fragment")
        case InputType.Model.GraphQLInput => List("graphql", "input")
        case InputType.Model.GraphQLMutation => List("graphql", "mutation")
        case InputType.Model.GraphQLQuery => List("graphql", "query")
        case InputType.Model.GraphQLReference => List("graphql", "reference")
        case _ => throw new IllegalStateException()
      }),
      propertyName = toIdentifier(cn),
      className = cn,
      title = title,
      description = None,
      plural = title + "s",
      arguments = arguments.toList,
      fields = fields.toList,
      source = source
    )
  }

  def modelFromSchema(pkg: Seq[String], schema: Schema[_, _], key: String, isInput: Boolean) = {
    schema.allTypes.get(key) match {
      case Some(t) => t match {
        case i: InputObjectType[_] =>
          val fields = i.fields.map(f => GraphQLFieldParser.getInputField(i.name, schema, f.name, f.fieldType))
          val it = if (isInput) { InputType.Model.GraphQLInput } else { InputType.Model.GraphQLReference }
          Seq(Right(modelFor(pkg, i.name, it, Nil, fields, None)))
        case o: ObjectType[_, _] =>
          val fields = o.fields.map(f => GraphQLFieldParser.getOutputField(o.name, schema, Document.emptyStub, f.name, f.fieldType, Nil))
          Seq(Right(modelFor(pkg, o.name, InputType.Model.GraphQLReference, Nil, fields, None)))
        case _ => throw new IllegalStateException(s"Invalid model type [$t]")
      }
      case _ =>
        val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
        throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
    }
  }

  def enumFromSchema(pkg: Seq[String], schema: Schema[_, _], key: String) = {
    schema.allTypes.get(key) match {
      case Some(t) => t match {
        case EnumType(name, _, values, _, _) => Seq(Left(ExportEnum(
          inputType = InputType.Enum.GraphQLEnum,
          pkg = pkg.toList ++ List("graphql", "enums"),
          key = name,
          className = toClassName(name),
          values = values.map(_.name)
        )))
        case _ => throw new IllegalStateException(s"Invalid enum type [$t]")
      }
      case _ =>
        val candidates = schema.allTypes.toSeq.filter(_._2.isInstanceOf[EnumType[_]]).map(_._1).sorted.mkString(", ")
        throw new IllegalStateException(s"Cannot find type [$key] in schema from candidates [$candidates]")
    }
  }

  def parseVariables(schema: Schema[_, _], doc: Document, variables: Seq[VariableDefinition]) = variables.map { v =>
    GraphQLFieldParser.getField(
      ctx = "",
      schema = schema,
      doc = doc,
      name = v.name,
      t = v.tpe,
      defaultValue = v.defaultValue
    )
  }
}
