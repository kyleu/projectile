package com.kyleu.projectile.models.feature.graphql.thrift

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ThriftModelSchemaFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, model.pkg, model.className + "Schema")

    config.addCommonImport(file, "GraphQLContext")

    file.addImport(model.pkg, model.className)

    file.addImport(Seq("sangria", "macros", "derive"), "AddFields")
    file.addImport(Seq("sangria", "macros", "derive"), "ObjectTypeName")
    file.addImport(Seq("sangria", "macros", "derive"), "InputObjectTypeName")
    file.addImport(Seq("sangria", "macros", "derive"), "deriveObjectType")
    file.addImport(Seq("sangria", "macros", "derive"), "deriveInputObjectType")
    file.addImport(Seq("sangria", "schema"), "_")
    file.addImport(Seq("sangria", "marshalling", "circe"), "_")

    ThriftSchemaInputHelper.addImports(pkg = model.pkg, types = model.fields.map(_.t), config = config, file = file)
    ThriftSchemaInputHelper.addInputImports(pkg = model.pkg, types = model.fields.map(_.t), config = config, file = file)

    file.add(s"""object ${model.className}Schema {""", 1)

    val deriveInput = s"deriveInputObjectType[${model.className}]"
    file.add(s"implicit lazy val ${model.propertyName}InputType: InputType[${model.className}] = $deriveInput(", 1)
    model.fields.foreach { f =>
      f.t match {
        case FieldType.MapType(_, _) =>
          file.addImport(Seq("sangria", "macros", "derive"), "ReplaceInputField")
          val t = ThriftSchemaInputHelper.graphQlInputTypeFor(f.t, config)
          file.add(s"""ReplaceInputField("${f.propertyName}", InputField("${f.propertyName}", $t)),""")
        case FieldType.SetType(_) =>
          file.addImport(Seq("sangria", "macros", "derive"), "ReplaceInputField")
          val t = ThriftSchemaInputHelper.graphQlInputTypeFor(f.t, config)
          file.add(s"""ReplaceInputField("${f.propertyName}", InputField("${f.propertyName}", $t)),""")
        case _ => // noop
      }
    }
    file.add(s"""InputObjectTypeName("Thrift${model.className}Input")""")
    file.add(")", -1)
    file.add()

    file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
    file.add(s"""ObjectTypeName("Thrift${model.className}"),""")
    model.fields.foreach { f =>
      f.t match {
        case FieldType.MapType(_, _) =>
          file.addImport(Seq("sangria", "macros", "derive"), "ReplaceField")
          config.addCommonImport(file, "JsonSerializers", "_")
          val t = if (f.required) { "StringType" } else { "OptionType(StringType)" }
          val extra = if (f.required) { ".asJson.spaces2" } else { ".map(_.asJson.spaces2)" }
          file.add(s"""ReplaceField("${f.propertyName}", Field("${f.propertyName}", $t, resolve = _.value.${f.propertyName}$extra)),""")
        case FieldType.SetType(_) =>
          file.addImport(Seq("sangria", "macros", "derive"), "ReplaceField")
          val t = ThriftSchemaHelper.graphQlTypeFor(f.t, config, req = f.required)
          val extra = if (f.required) { ".toSeq" } else { ".map(_.toSeq)" }
          file.add(s"""ReplaceField("${f.propertyName}", Field("${f.propertyName}", $t, resolve = _.value.${f.propertyName}$extra)),""")
        case _ => // noop
      }
    }
    file.add(s"AddFields(Field(", 1)
    file.add("""name = "toString",""")
    file.add("fieldType = StringType,")
    file.add("resolve = c => c.value.toString")
    file.add("))", -1)
    file.add(")", -1)

    file.add("}", -1)

    file
  }
}
