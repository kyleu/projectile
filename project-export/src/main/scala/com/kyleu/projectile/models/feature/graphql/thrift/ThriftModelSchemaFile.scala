package com.kyleu.projectile.models.feature.graphql.thrift

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object ThriftModelSchemaFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerSource, model.pkg :+ "graphql", model.className + "Schema")

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
    file.add(s"""InputObjectTypeName("Thrift${model.className}Input")""")
    file.add(")", -1)
    file.add()

    file.add(s"implicit lazy val ${model.propertyName}Type: ObjectType[GraphQLContext, ${model.className}] = deriveObjectType(", 1)
    file.add(s"""ObjectTypeName("Thrift${model.className}"),""")
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
