package services.project

import models.database.schema.ColumnType
import models.export.config.ExportConfiguration
import util.JsonSerializers._

object ProjectAuditService {
  object AuditResult {
    implicit val jsonEncoder: Encoder[AuditResult] = deriveEncoder
    implicit val jsonDecoder: Decoder[AuditResult] = deriveDecoder
  }

  case class AuditResult(
      srcModel: String,
      src: String,
      t: String,
      tgt: String,
      message: String
  )

  def audit(c: ExportConfiguration) = {
    c.models.flatMap { m =>
      val missingEnums = m.fields.filter(_.t == ColumnType.EnumType).flatMap(f => f.enumOpt(c) match {
        case None => Some(AuditResult(
          srcModel = m.key, src = f.key, t = "enum", tgt = f.sqlTypeName, message = "Missing enum definition"
        ))
        case _ => None
      })
      val missingModels = m.references.flatMap {
        case r if c.getModelOpt(r.srcTable).isEmpty => Some(AuditResult(
          srcModel = m.key, src = r.name, t = "model", tgt = r.srcTable, message = "Missing model definition"
        ))
        case _ => None
      }
      missingEnums ++ missingModels
    }
  }
}
