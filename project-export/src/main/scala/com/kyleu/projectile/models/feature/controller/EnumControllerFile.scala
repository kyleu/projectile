package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object EnumControllerFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, enum.controllerPackage(config), enum.className + "Controller")
    file.addImport(enum.modelPackage(config), enum.className)

    config.addCommonImport(file, "Application")
    config.addCommonImport(file, "BaseController")
    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "Implicits", "_")
    config.addCommonImport(file, "ServiceController")

    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("play", "twirl", "api"), "Html")

    val prefix = config.applicationPackage.map(_ + ".").mkString

    file.add("@javax.inject.Singleton")
    val constructorArgs = s"@javax.inject.Inject() (override val app: Application)"
    file.add(s"""class ${enum.className}Controller $constructorArgs extends BaseController("${enum.propertyName}") {""", 1)
    file.add()
    file.add(s"""def list = withSession("list", admin = true) { implicit request => implicit td =>""", 1)
    file.add(s"Future.successful(render {", 1)
    val listArgs = s"""request.identity, "${enum.className}", "explore""""
    file.add(s"case Accepts.Html() => Ok(${prefix}views.html.admin.layout.listPage($listArgs, ${enum.className}.values.map(v => Html(v.toString))))")
    file.add(s"""case Accepts.Json() => Ok(${enum.className}.values.asJson)""")
    file.add(s"""case ServiceController.acceptsCsv() => Ok(${enum.className}.values.mkString(", ")).as("text/csv")""")
    file.add(s"})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
