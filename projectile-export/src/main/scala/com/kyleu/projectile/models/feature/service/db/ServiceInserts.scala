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
    if (model.isSerial) { insertSerial(config, file, model, queriesFilename) } else { insertNormal(config, file, model, queriesFilename) }
    file.add("}", -1)

    file.add(s"""def insertBatch(creds: Credentials, models: Seq[${model.className}], $conn)(implicit trace: TraceData) = $editCheck{""", 1)
    file.add(s"""traceF("insertBatch")(td => if (models.isEmpty) {""")
    file.add(s"  Future.successful(0)")
    file.add(s"} else {", 1)
    file.add(s"Future.sequence(models.grouped(100).zipWithIndex.map { batch =>", 1)
    file.add(s"""log.info(s"Processing batch [$${batch._2}]...")""")
    file.add(s"db.executeF($queriesFilename.insertBatch(batch._1), conn)(td)")
    file.add(s"}).map(_.sum)", -1)
    file.add("})", -1)
    file.add("}", -1)

    file.add(s"""def create(creds: Credentials, fields: Seq[DataField], $conn)(implicit trace: TraceData) = $editCheck{""", 1)
    if (model.isSerial) { createSerial(config, file, model, queriesFilename) } else { createNormal(config, file, model, queriesFilename) }
    file.add("}", -1)
  }

  private[this] def insertNormal(config: ExportConfiguration, file: ScalaFile, model: ExportModel, queriesFilename: String) = {
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
  }

  private[this] def insertSerial(config: ExportConfiguration, file: ScalaFile, model: ExportModel, queriesFilename: String) = {
    file.add(s"""traceF("insert")(td => db.queryF($queriesFilename.insert(model), conn)(td).flatMap {""", 1)
    config.addCommonImport(file, "DatabaseFieldType")
    val coerced = model.pkFields.zipWithIndex.map { pk =>
      val ref = if (pk._2 == 0) { "pks.headOption.getOrElse(throw new IllegalStateException())" } else { s"pks(${pk._2}" }
      s"DatabaseFieldType.${QueriesHelper.classNameForSqlType(pk._1.t, config)}.coerce($ref)"
    }.mkString(", ")
    if (model.features(ModelFeature.Audit)) {
      config.addCommonImport(file, "AuditHelper")
      file.add(s"case Some(pks) => getByPrimaryKey(creds, $coerced, conn)(td).map(_.map { n =>", 1)
      val audit = model.pkFields.map(f => "n." + f.propertyName + ".toString").mkString(", ")
      file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
      file.add("n")
      file.add("})", -1)
    } else {
      file.add(s"case Some(pks) => getByPrimaryKey(creds, $coerced, conn)(td)")
    }
    file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}")""")
    file.add("})", -1)
  }

  def createNormal(config: ExportConfiguration, file: ScalaFile, model: ExportModel, queriesFilename: String) = {
    file.add(s"""traceF("create")(td => db.executeF($queriesFilename.create(fields), conn)(td).flatMap { _ =>""", 1)
    model.pkFields match {
      case Nil => file.add(s"Future.successful(None: Option[${model.className}])")
      case pk =>
        if (pk.exists(x => x.t.isDate)) {
          file.addImport(CommonImportHelper.get(config, "DateUtils")._1, "DateUtils")
        }
        val fieldVals = pk.map(k => FieldTypeFromString.fromString(config, k.t, s"""fieldVal(fields, "${k.propertyName}")""")).mkString(", ")
        val fieldLookups = pk.map(k => s"""fields.exists(_.k == "${k.propertyName}")""")

        file.add(s"if (${fieldLookups.mkString(" && ")}) {", 1)
        if (model.features(ModelFeature.Audit)) {
          val audit = pk.map(k => s"""fieldVal(fields, "${k.propertyName}")""").mkString(", ")
          file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), fields, creds)""")
        }
        file.add(s"getByPrimaryKey(creds, $fieldVals, conn)")
        file.add("} else {", -1)
        file.indent()
        file.add(s"Future.successful(None)")
        file.add("}", -1)

    }
    file.add("})", -1)
  }

  def createSerial(config: ExportConfiguration, file: ScalaFile, model: ExportModel, queriesFilename: String) = {
    file.add(s"""traceF("create")(td => db.queryF($queriesFilename.create(fields), conn)(td).flatMap {""", 1)
    val coerced = model.pkFields.zipWithIndex.map { pk =>
      val ref = if (pk._2 == 0) { "pks.headOption.getOrElse(throw new IllegalStateException())" } else { s"pks(${pk._2}" }
      s"DatabaseFieldType.${QueriesHelper.classNameForSqlType(pk._1.t, config)}.coerce($ref)"
    }.mkString(", ")
    model.pkFields match {
      case Nil => file.add(s"case _ => Future.successful(None: Option[${model.className}])")
      case pks if (model.features(ModelFeature.Audit)) =>
        file.add(s"""case Some(pks) => getByPrimaryKey(creds, $coerced, conn)(td).map(_.map { n =>""", 1)
        val audit = pks.map(k => s"""n.${k.propertyName}.toString""").mkString(", ")
        file.add(s"""AuditHelper.onInsert("${model.className}", Seq($audit), n.toDataFields, creds)""")
        file.add("n")
        file.add("})", -1)
        file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}")""")
      case _ =>
        file.add(s"""case Some(pks) => getByPrimaryKey(creds, $coerced, conn)(td)""")
        file.add(s"""case _ => throw new IllegalStateException("Unable to find newly-inserted ${model.title}")""")
    }
    file.add("})", -1)
  }

}
