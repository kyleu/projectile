package services.project.audit

import models.database.schema.ColumnType
import models.export.ExportModel
import models.export.config.ExportConfiguration
import models.project.ProjectOutput
import models.project.audit.{AuditMessage, AuditResult}

object ProjectAuditService {
  def audit(c: ExportConfiguration, o: ProjectOutput) = {
    val missing = c.models.flatMap(checkMissing(c, _))

    AuditResult(config = c, configMessages = missing, output = o, outputMessages = Nil)
  }

  private[this] def checkMissing(c: ExportConfiguration, m: ExportModel) = {
    val missingEnums = m.fields.filter(_.t == ColumnType.EnumType).flatMap(f => f.enumOpt(c) match {
      case None => Some(AuditMessage(
        srcModel = m.key, src = f.key, t = "enum", tgt = f.sqlTypeName, message = "Missing enum definition"
      ))
      case _ => None
    })
    val missingModels = m.references.flatMap {
      case r if c.getModelOpt(r.srcTable).isEmpty => Some(AuditMessage(
        srcModel = m.key, src = r.name, t = "model", tgt = r.srcTable, message = "Missing model definition"
      ))
      case _ => None
    }
    missingEnums ++ missingModels
  }
}
