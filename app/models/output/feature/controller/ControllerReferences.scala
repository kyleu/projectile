package models.output.feature.controller

import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.output.file.ScalaFile

object ControllerReferences {
  def write(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    val references = model.transformedReferencesDistinct(config)
    if (references.nonEmpty) {
      file.addImport(config.applicationPackage ++ Seq("models", "auth"), "Credentials")
      file.addImport(config.systemPackage ++ Seq("models", "result"), "RelationCount")
      val pkRefs = model.pkFields.map(_.propertyName).mkString(", ")
      val pkArgs = model.pkFields.map(x => s"${x.propertyName}: ${x.scalaType(config)}").mkString(", ")
      val refServices = references.map(ref => (ref._2, ref._3, ref._4))

      file.add()
      file.add(s"""def relationCounts($pkArgs) = withSession("relation.counts", admin = true) { implicit request => implicit td =>""", 1)
      file.add(s"val creds = Credentials.fromRequest(request)")

      refServices.foreach { r =>
        file.add(s"val ${r._2.propertyName}By${r._3.className}F = ${r._2.propertyName}S.countBy${r._3.className}(creds, $pkRefs)")
      }
      val forArgs = refServices.map(r => s"${r._2.propertyName}C <- ${r._2.propertyName}By${r._3.className}F").mkString("; ")
      file.add(s"for ($forArgs) yield {", 1)

      file.add(s"Ok(Seq(", 1)
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
    val refServices = model.validReferences(config).map(_.srcTable).distinct.map(config.getModel)
    refServices.foreach(s => file.addImport(config.applicationPackage ++ s.servicePackage, s.className + "Service"))
    refServices.map(s => s.propertyName + "S: " + s.className + "Service").mkString(", ")
  }
}
