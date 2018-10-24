package services.project

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
      val missingEnums = m.fields.flatMap(f => f.enumOpt.flatMap {
        case e if c.getEnumOpt(e.name).isEmpty => Some(AuditResult(
          srcModel = m.name, src = f.columnName, t = "enum", tgt = e.name, message = "Missing enum definition"
        ))
        case _ => None
      })
      val missingModels = m.references.flatMap {
        case r if c.getModelOpt(r.srcTable).isEmpty => Some(AuditResult(
          srcModel = m.name, src = r.name, t = "model", tgt = r.srcTable, message = "Missing model definition"
        ))
        case _ => None
      }
      missingEnums ++ missingModels
    }
  }
}
