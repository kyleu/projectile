package com.kyleu.projectile.services.project.audit

import com.kyleu.projectile.models.export.ExportModel
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType.EnumType
import com.kyleu.projectile.models.project.ProjectOutput
import com.kyleu.projectile.models.project.audit.{AuditMessage, AuditResult}
import com.kyleu.projectile.services.ProjectileService

object ProjectAuditService {
  def audit(svc: ProjectileService, inputs: Seq[(ExportConfiguration, ProjectOutput)]) = {
    val missing = inputs.flatMap(i => i._1.models.flatMap(checkMissing(i._1, _)))

    val configMessages = missing ++ getDupes(inputs)

    val orphans = ExportValidation.validate(svc = svc, results = inputs.map(_._2)).map { valResult =>
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

  private[this] def getDupes(inputs: Seq[(ExportConfiguration, ProjectOutput)]) = {
    def msgForDupe(k: String, dupe: Seq[String]) = {
      val msg = s"There are [${dupe.size}] generated classes with $k [${dupe.head}]"
      AuditMessage(project = "all", srcModel = dupe.head, src = dupe.head, t = "duplicate", tgt = dupe.head, message = msg)
    }

    val dupeClassnames = inputs.flatMap { i =>
      (i._1.models.map(_.className) ++ i._1.enums.map(_.className)).groupBy(x => x).values.filter(_.size > 1).map(msgForDupe("className", _))
    }

    val dupeKeys = inputs.flatMap { i =>
      (i._1.models.map(_.key) ++ i._1.enums.map(_.key)).groupBy(x => x).values.filter(_.size > 1).map(msgForDupe("key", _))
    }

    dupeClassnames ++ dupeKeys
  }
}
