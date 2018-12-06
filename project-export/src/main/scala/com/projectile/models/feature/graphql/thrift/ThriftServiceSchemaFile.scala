package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.{ExportMethod, ExportService}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType
import com.projectile.models.output.OutputPath
import com.projectile.models.output.file.ScalaFile

object ThriftServiceSchemaFile {
  def export(config: ExportConfiguration, service: ExportService) = {
    val file = ScalaFile(path = OutputPath.ServerSource, service.pkg :+ "graphql", service.className + "Schema")

    file.addImport(service.pkg, service.className)

    config.addCommonImport(file, "GraphQLSchemaHelper")
    config.addCommonImport(file, "GraphQLContext")
    config.addCommonImport(file, "FutureUtils", "graphQlContext")
    file.addImport(Seq("sangria", "schema"), "_")
    file.addImport(Seq("sangria", "marshalling", "circe"), "_")

    file.add(s"""object ${service.className}Schema extends GraphQLSchemaHelper("${service.key}") {""", 1)

    file.add(s"private[this] val ${service.propertyName}Fields = fields[GraphQLContext, ${service.className}](", 1)
    service.methods.foreach(m => addMethodField(service.pkg, m, config, file))
    file.add("Field(", 1)
    file.add("""name = "healthcheck",""")
    file.add("fieldType = StringType,")
    file.add(s"""resolve = c => traceF(c.ctx, "healthcheck")(td => c.value.healthcheck(td))""")
    file.add(")", -1)
    file.add(")", -1)
    file.add()
    val objType = s"""ObjectType(name = "${service.className}", fields = ${service.propertyName}Fields)"""
    file.add(s"lazy val ${service.className}Type = $objType")
    file.add("}", -1)

    file
  }

  private[this] def addMethodField(pkg: Seq[String], m: ExportMethod, config: ExportConfiguration, file: ScalaFile) = {
    ThriftSchemaInputHelper.addImports(pkg = pkg, types = m.args.map(_.t) :+ m.returnType, config = config, file = file)

    val retGraphQlType = ThriftSchemaHelper.graphQlTypeFor(m.returnType, config)

    if (m.args.isEmpty) {
      file.add("Field(", 1)
      file.add(s"""name = "${m.key}",""")
      file.add(s"fieldType = $retGraphQlType,")
      file.add(s"""resolve = c => traceF(c.ctx, "${m.key}")(td => c.value.${m.key}()(td))""")
      file.add("),", -1)
    } else {
      file.add("{", 1)
      ThriftSchemaInputHelper.addInputImports(pkg = pkg, types = m.args.map(_.t), config = config, file = file)
      m.args.foreach { arg =>
        val argInputType = ThriftSchemaInputHelper.graphQlInputTypeFor(arg.t, config)
        val optionInputType = if (arg.notNull) {
          argInputType
        } else {
          s"OptionInputType($argInputType)"
        }
        file.add(s"""val ${arg.key}Arg = Argument(name = "${arg.key}", argumentType = $optionInputType)""")
      }
      file.add("Field(", 1)
      file.add(s"""name = "${m.key}",""")
      file.add(s"fieldType = $retGraphQlType,")
      file.add(s"arguments = ${m.args.map(arg => arg.key + "Arg :: ").mkString}Nil,")

      file.add(s"""resolve = c => traceF(c.ctx, "${m.key}") { td =>""", 1)
      val argsRefs = m.args.map { arg =>
        val extras = arg.t match {
          case FieldType.ListType(_) => ".toList"
          case FieldType.SetType(_) => ".toSet"
          case _ => ""
        }
        s"${arg.key} = c.arg(${arg.key}Arg)$extras"
      }
      val extras = m.returnType match {
        case FieldType.UnitType => ".map(_ => \"OK\")"
        case _ => ""
      }
      file.add(s"c.value.${m.key}(${argsRefs.mkString(", ")})(td)$extras")
      file.add("}", -1)
      file.add(")", -1)
      file.add("},", -1)
    }
  }
}
