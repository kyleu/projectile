package com.kyleu.projectile.models.feature.doobie

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.ScalaFile

object DoobieTestsFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ScalaFile(path = OutputPath.ServerTest, dir = model.doobiePackage(config), key = model.className + "DoobieTests")

    file.addImport(Seq("org", "scalatest"), "_")
    file.addImport(model.modelPackage(config), model.className)
    config.addCommonImport(file, "DoobieQueryService", "Imports", "_")

    model.fields.foreach { field =>
      DoobieImports.imports(config, field.t).foreach(pkg => file.addImport(pkg.dropRight(1), pkg.lastOption.getOrElse(throw new IllegalStateException())))
    }

    file.add(s"class ${model.className}DoobieTests extends FlatSpec with Matchers {", 1)
    config.addCommonImport(file, "DoobieTestHelper", "yolo", "_")

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
