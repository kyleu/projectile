package models.output.feature.doobie

import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.OutputPath
import models.output.file.ScalaFile

object DoobieTestsFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerTest, dir = config.applicationPackage ++ model.doobiePackage, key = model.className + "DoobieTests")

    file.addImport("org.scalatest", "_")
    file.addImport((config.applicationPackage ++ model.modelPackage).mkString("."), model.className)
    file.addImport((config.systemPackage ++ Seq("services", "database", "doobie", "DoobieQueryService", "Imports")).mkString("."), "_")

    model.fields.foreach(_.enumOpt(config).foreach { e =>
      file.addImport(s"${(config.applicationPackage ++ e.doobiePackage).mkString(".")}.${e.className}Doobie", s"${e.propertyName}Meta")
    })

    file.add(s"class ${model.className}DoobieTests extends FlatSpec with Matchers {", 1)
    file.addImport((config.systemPackage ++ Seq("models", "doobie", "DoobieTestHelper", "yolo")).mkString("."), "_")

    file.add()
    file.add(s""""Doobie queries for [${model.className}]" should "typecheck" in {""", 1)
    file.add(s"${model.className}Doobie.countFragment.query[Long].check.unsafeRunSync")
    file.add(s"${model.className}Doobie.selectFragment.query[${model.className}].check.unsafeRunSync")
    val search = s"""(${model.className}Doobie.selectFragment ++ whereAnd(${model.className}Doobie.searchFragment("...")))"""
    file.add(s"$search.query[${model.className}].check.unsafeRunSync")
    file.add("}", -1)

    file.add("}", -1)

    file
  }
}
