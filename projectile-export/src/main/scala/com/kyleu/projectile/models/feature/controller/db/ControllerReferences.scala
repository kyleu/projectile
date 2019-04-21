package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.{ExportModel, ExportModelReference}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerReferences {
  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val references = ExportModelReference.transformedReferences(config, model)
    if (references.nonEmpty) {
      config.addCommonImport(file, "RelationCount")

      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")

      file.add()
      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", admin = true) { implicit request => implicit td =>""", 1)

      references.foreach { r =>
        file.add(s"val ${r.src.propertyName}By${r.tf.className}F = ${r.src.propertyName}S.countBy${r.tf.className}(request, $pkRefs)")
      }
      val forArgs = references.map(r => s"${r.src.propertyName}By${r.tf.className}C <- ${r.src.propertyName}By${r.tf.className}F").mkString("; ")
      file.add(s"for ($forArgs) yield {", 1)

      file.add("Ok(Seq(", 1)
      references.foreach { r =>
        val count = s"${r.src.propertyName}By${r.tf.className}C"
        val comma = if (references.lastOption.contains(r)) { "" } else { "," }
        file.add(s"""RelationCount(model = "${r.src.propertyName}", field = "${r.tf.propertyName}", count = $count)$comma""")
      }
      file.add(").asJson)", -1)
      file.add("}", -1)
      file.add("}", -1)
    }
  }

  def refServiceArgs(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val refServices = ExportModelReference.validReferences(config, model).map(_.srcTable).distinct.map(m => config.getModel(m, "reference args"))
    refServices.foreach(s => file.addImport(s.servicePackage(config), s.className + "Service"))
    refServices.map(s => s.propertyName + "S: " + s.className + "Service").mkString(", ")
  }
}
