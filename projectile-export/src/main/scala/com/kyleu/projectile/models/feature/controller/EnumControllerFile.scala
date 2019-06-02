package com.kyleu.projectile.models.feature.controller

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.feature.EnumFeature
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object EnumControllerFile {
  def export(config: ExportConfiguration, enum: ExportEnum) = {
    val file = ScalaFile(path = OutputPath.ServerSource, enum.controllerPackage(config), enum.className + "Controller")
    file.addImport(enum.modelPackage(config), enum.className)

    config.addCommonImport(file, "Application")

    if (enum.features(EnumFeature.Auth)) {
      config.addCommonImport(file, "AuthController")
    } else {
      config.addCommonImport(file, "BaseController")
    }
    config.addCommonImport(file, "JsonSerializers", "_")
    config.addCommonImport(file, "ExecutionContext")
    config.addCommonImport(file, "BaseController")

    file.addImport(Seq("scala", "concurrent"), "Future")
    file.addImport(Seq("play", "twirl", "api"), "Html")

    val prefix = config.systemPackage.map(_ + ".").mkString

    file.add("@javax.inject.Singleton")
    val constructorArgs = "@javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext)"
    val controller = if (enum.features(EnumFeature.Auth)) { "AuthController" } else { "BaseController" }
    file.add(s"""class ${enum.className}Controller $constructorArgs extends $controller("${enum.propertyName}") {""", 1)
    file.add(s"""def list = withSession("list", ${enum.perm}) { implicit request => implicit td =>""", 1)
    file.add("Future.successful(render {", 1)
    file.add(s"case Accepts.Html() => Ok(${prefix}views.html.admin.layout.listPage(", 1)
    file.add(s"""title = "${enum.className}",""")
    file.add(s"""cfg = app.cfg(u = Some(request.identity), "${enum.firstPackage}", "${enum.key}"),""")
    file.add(s"vals = ${enum.className}.values.map(v => Html(v.toString))")
    file.add("))", -1)
    file.add(s"""case Accepts.Json() => Ok(${enum.className}.values.asJson)""")
    file.add(s"""case BaseController.acceptsCsv() => Ok(${enum.className}.values.mkString(", ")).as("text/csv")""")
    file.add("})", -1)
    file.add("}", -1)
    file.add("}", -1)
    file
  }
}
