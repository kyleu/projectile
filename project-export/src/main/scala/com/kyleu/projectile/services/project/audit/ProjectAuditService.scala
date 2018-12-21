package com.kyleu.projectile.services.project.audit

import better.files.File
import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType.EnumType
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.models.project.audit.{AuditMessage, AuditResult}

object ProjectAuditService {
  def audit(projectRoot: File, inputs: Seq[(ExportConfiguration, ProjectOutput)]) = {
    val configMessages = inputs.flatMap { i =>
      val missing = i._1.models.flatMap(checkMissing(i._1, _))
      missing
    }

    val orphans = ExportValidation.validate(projectRoot = projectRoot, results = inputs.map(_._2)).map { valResult =>
      AuditMessage(project = "all", srcModel = valResult._1, src = valResult._1, t = "orphan", tgt = valResult._1, message = valResult._2)
    }
    val outputMessages = orphans

    AuditResult(configMessages = configMessages, outputMessages = outputMessages)
  }

  private[this] def checkMissing(c: ExportConfiguration, m: ExportModel) = {
    val missingEnums = m.fields.flatMap(_.t match {
      case EnumType(key) => c.getEnumOpt(key) match {
        case None => Some(AuditMessage(
          project = c.project.key, srcModel = m.key, src = key, t = "enum", tgt = key, message = "Missing enum definition"
        ))
        case _ => None
      }
      case _ => None
    })

    val missingModels = m.references.flatMap {
      case r if c.getModelOpt(r.srcTable).isEmpty => Some(AuditMessage(
        project = c.project.key, srcModel = m.key, src = r.name, t = "model", tgt = r.srcTable, message = "Missing model definition"
      ))
      case _ => None
    }
    missingEnums ++ missingModels
  }
}
