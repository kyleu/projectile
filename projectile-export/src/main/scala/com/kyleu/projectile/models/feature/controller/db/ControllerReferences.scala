package com.kyleu.projectile.models.feature.controller.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.ScalaFile

object ControllerReferences {
  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val references = model.transformedReferencesDistinct(config)
    if (references.nonEmpty) {
      config.addCommonImport(file, "RelationCount")

      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(f => s"${f.propertyName}: ${f.scalaType(config)}").mkString(", ")
      val refServices = references.map(ref => (ref._2, ref._3, ref._4))

      file.add()
      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", admin = true) { implicit request => implicit td =>""", 1)

      refServices.foreach { r =>
        file.add(s"val ${r._2.propertyName}By${r._3.className}F = ${r._2.propertyName}S.countBy${r._3.className}(request, $pkRefs)")
      }
      val forArgs = refServices.map(r => s"${r._2.propertyName}C <- ${r._2.propertyName}By${r._3.className}F").mkString("; ")
      file.add(s"for ($forArgs) yield {", 1)

      file.add("Ok(Seq(", 1)
      refServices.foreach { r =>
        val comma = if (refServices.lastOption.contains(r)) { "" } else { "," }
        file.add(s"""RelationCount(model = "${r._2.propertyName}", field = "${r._3.propertyName}", count = ${r._2.propertyName}C)$comma""")
      }
      file.add(").asJson)", -1)
      file.add("}", -1)
      file.add("}", -1)
    }
  }

  def refServiceArgs(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val refServices = model.validReferences(config).map(_.srcTable).distinct.map(m => config.getModel(m, "reference args"))
    refServices.foreach(s => file.addImport(s.servicePackage(config), s.className + "Service"))
    refServices.map(s => s.propertyName + "S: " + s.className + "Service").mkString(", ")
  }
}
