package com.projectile.models.graphql.parse

import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.ModelMember.InputType
import sangria.ast._
import sangria.schema.Schema

object GraphQLDocumentParser {
  def parse(schema: Schema[_, _], doc: Document) = {
    val fragments = doc.fragments.map(f => parseFragment(schema, f._1, f._2))
    val inputs = doc.definitions.collect { case x: InputObjectTypeDefinition => x }.map(i => parseInput(schema, i))
    val mutations = doc.operations.filter(_._2.operationType == OperationType.Mutation).map(q => parseMutation(schema, q._1, q._2))
    val queries = doc.operations.filter(_._2.operationType == OperationType.Query).map(q => parseQuery(schema, q._1, q._2))

    (fragments ++ inputs ++ mutations ++ queries).toSeq
  }

  private[this] def parseFragment(schema: Schema[_, _], key: String, f: FragmentDefinition) = {
    val fields = GraphQLSelectionParser.fieldsForSelections(schema, f.selections)
    modelFor(key, InputType.GraphQLFragment, fields)
  }

  private[this] def parseInput(schema: Schema[_, _], i: InputObjectTypeDefinition) = {
    val fields = i.fields.zipWithIndex.map(f => GraphQLFieldParser.getField(schema, f._1.name, f._1.valueType, f._2, f._1.defaultValue))
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
}
