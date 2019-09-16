package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeFromString
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.feature.service.db.ServiceHelper.conn
import com.kyleu.projectile.models.output.CommonImportHelper
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceInserts {
  def insertsFor(config: ExportConfiguration, model: ExportModel, queriesFilename: String, file: ScalaFile) = {
    val editCheck = if (model.features(ModelFeature.Auth)) { """checkPerm(creds, "edit") """ } else { "" }
    file.add("// Mutations")
    file.add(s"""def insert(creds: Credentials, model: ${model.className}, $conn)(implicit trace: TraceData) = $editCheck{""", 1)
    file.add(s"""traceF("insert")(td => db.executeF($queriesFilename.insert(model), conn)(td).flatMap {""", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      if (model.features(ModelFeature.Audit)) {
        config.addCommonImport(file, "AuditHelper")
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")}, conn)(td).map(_.map { n =>", 1)
        val audit = model.pkFields.map(f => "n." + f.propertyName + ".toString").mkString(", ")
        file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
        file.add("n")
        file.add("})", -1)
      } else {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")}, conn)(td)")
      }
      file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}")""")
    }
    file.add("})", -1)
    file.add("}", -1)

    file.add(s"""def insertBatch(creds: Credentials, models: Seq[${model.className}], $conn)(implicit trace: TraceData) = $editCheck{""", 1)
    file.add(s"""traceF("insertBatch")(td => if (models.isEmpty) {""")
    file.add(s"  Future.successful(0)")
    file.add(s"} else {")
    file.add(s"  db.executeF($queriesFilename.insertBatch(models), conn)(td)")
    file.add("})")
    file.add("}", -1)

    file.add(s"""def create(creds: Credentials, fields: Seq[DataField], $conn)(implicit trace: TraceData) = $editCheck{""", 1)
    file.add(s"""traceF("create")(td => db.executeF($queriesFilename.create(fields), conn)(td).flatMap { _ =>""", 1)
    model.pkFields match {
      case Nil => file.add(s"Future.successful(None: Option[${model.className}])")
      case pk =>
        if (pk.exists(x => x.t.isDate)) {
          file.addImport(CommonImportHelper.get(config, "DateUtils")._1, "DateUtils")
        }
        val lookup = pk.map(k => FieldTypeFromString.fromString(config, k.t, s"""fieldVal(fields, "${k.propertyName}")""")).mkString(", ")
        if (model.features(ModelFeature.Audit)) {
          val audit = pk.map(k => s"""fieldVal(fields, "${k.propertyName}")""").mkString(", ")
          file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), fields, creds)""")
        }
        file.add(s"getByPrimaryKey(creds, $lookup, conn)")
    }
    file.add("})", -1)
    file.add("}", -1)
  }
}
