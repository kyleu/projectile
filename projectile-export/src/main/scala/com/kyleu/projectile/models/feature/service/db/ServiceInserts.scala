package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeFromString
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceInserts {
  def insertsFor(config: ExportConfiguration, model: ExportModel, queriesFilename: String, file: ScalaFile) = {
    file.add("// Mutations")
    file.add(s"""def insert(creds: Credentials, model: ${model.className})(implicit trace: TraceData) = checkPerm(creds, "edit") {""", 1)
    file.add(s"""traceF("insert")(td => db.executeF($queriesFilename.insert(model))(td).flatMap {""", 1)
    if (model.pkFields.isEmpty) {
      file.add(s"case _ => scala.concurrent.Future.successful(None: Option[${model.className}])")
    } else {
      if (model.features(ModelFeature.Audit)) {
        config.addCommonImport(file, "AuditHelper")
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td).map(_.map { n =>", 1)
        val audit = model.pkFields.map(f => "n." + f.propertyName + ".toString").mkString(", ")
        file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
        file.add("n")
        file.add("})", -1)
      } else {
        file.add(s"case 1 => getByPrimaryKey(creds, ${model.pkFields.map(f => "model." + f.propertyName).mkString(", ")})(td)")
      }
      file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}.")""")
    }
    file.add("})", -1)
    file.add("}", -1)

    file.add(s"""def insertBatch(creds: Credentials, models: Seq[${model.className}])(implicit trace: TraceData) = checkPerm(creds, "edit") {""", 1)
    file.add(s"""traceF("insertBatch")(td => db.executeF($queriesFilename.insertBatch(models))(td))""")
    file.add("}", -1)

    file.add("""def create(creds: Credentials, fields: Seq[DataField])(implicit trace: TraceData) = checkPerm(creds, "edit") {""", 1)
    file.add(s"""traceF("create")(td => db.executeF($queriesFilename.create(fields))(td).flatMap { _ =>""", 1)
    model.pkFields match {
      case Nil => file.add(s"Future.successful(None: Option[${model.className}])")
      case pk =>
        val lookup = pk.map(k => FieldTypeFromString.fromString(config, k.t, s"""fieldVal(fields, "${k.propertyName}")""")).mkString(", ")
        if (model.features(ModelFeature.Audit)) {
          val audit = pk.map(k => s"""fieldVal(fields, "${k.propertyName}")""").mkString(", ")
          file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), fields, creds)""")
        }
        file.add(s"getByPrimaryKey(creds, $lookup)")
    }
    file.add("})", -1)
    file.add("}", -1)
  }
}
