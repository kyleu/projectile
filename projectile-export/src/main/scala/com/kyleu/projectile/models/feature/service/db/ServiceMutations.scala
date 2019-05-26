package com.kyleu.projectile.models.feature.service.db

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldTypeFromString
import com.kyleu.projectile.models.feature.ModelFeature
import com.kyleu.projectile.models.output.file.ScalaFile

object ServiceMutations {
  private[this] val trace = "(implicit trace: TraceData)"

  def mutations(config: ExportConfiguration, model: ExportModel, file: ScalaFile) = {
    if (model.pkFields.nonEmpty) {
      model.pkFields.foreach(_.addImport(config, file, Nil))
      val sig = model.pkFields.map(f => f.propertyName + ": " + f.scalaType(config)).mkString(", ")
      val call = model.pkFields.map(_.propertyName).mkString(", ")
      val interp = model.pkFields.map("$" + _.propertyName).mkString(", ")
      file.addImport(Seq("scala", "concurrent"), "Future")
      file.add()
      file.add(s"def remove(creds: Credentials, $sig)$trace = {", 1)
      file.add(s"""traceF("remove")(td => getByPrimaryKey(creds, $call)(td).flatMap {""", 1)
      file.add("case Some(current) =>", 1)
      if (model.features(ModelFeature.Audit)) {
        config.addCommonImport(file, "AuditHelper")
        val audit = model.pkFields.map(f => f.propertyName + ".toString").mkString(", ")
        file.add(s"""AuditHelper.onRemove("${model.className}", Seq($audit), current.toDataFields, creds)""")
      }
      file.add(s"db.executeF(${model.className}Queries.removeByPrimaryKey($call))(td).map(_ => current)")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp]")""")
      file.add("})", -1)
      file.add("}", -1)
      file.add()

      file.add(s"def update(creds: Credentials, $sig, fields: Seq[DataField])$trace = {", 1)
      file.add(s"""traceF("update")(td => getByPrimaryKey(creds, $call)(td).flatMap {""", 1)
      file.add(s"""case Some(current) if fields.isEmpty => Future.successful(current -> s"No changes required for ${model.title} [$interp]")""")
      val currName = if (model.features(ModelFeature.Audit)) { "current" } else { "_" }
      file.add(s"case Some($currName) => db.executeF(${model.className}Queries.update($call, fields))(td).flatMap { _ =>", 1)

      val newCall = model.pkFields.map { f =>
        s"""fields.find(_.k == "${f.propertyName}").flatMap(_.v).map(s => ${FieldTypeFromString.fromString(config, f.t, "s")}).getOrElse(${f.propertyName})"""
      }.mkString(", ")
      file.add(s"getByPrimaryKey(creds, $newCall)(td).map {", 1)
      file.add("case Some(newModel) =>", 1)
      val ids = model.pkFields.map {
        case f if f.required => s"""${f.propertyName}.toString"""
        case f => s"""${f.propertyName}.map(_.toString).getOrElse("unknown")"""
      }.mkString(", ")
      if (model.features(ModelFeature.Audit)) {
        file.add(s"""AuditHelper.onUpdate("${model.className}", Seq($ids), current.toDataFields, fields, creds)""")
      }
      file.add(s"""newModel -> s"Updated [$${fields.size}] fields of ${model.title} [$interp]"""")
      file.indent(-1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp]")""")
      file.add("}", -1)
      file.add("}", -1)
      file.add(s"""case None => throw new IllegalStateException(s"Cannot find ${model.className} matching [$interp]")""")
      file.add("})", -1)
      file.add("}", -1)
    }
  }
}
