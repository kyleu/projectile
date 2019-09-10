package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.output.OutputPackage
import com.kyleu.projectile.models.project.member.{EnumMember, MemberOverride, ModelMember, ServiceMember, UnionMember}
import com.kyleu.projectile.models.project.{Project, ProjectSummary}
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.JsonFileLoader
import com.kyleu.projectile.util.JsonSerializers.Decoder
import io.scalaland.chimney.dsl._

object ProjectLoadService {
  object Provided {
    val models = Map(
      "audit" -> (("audit", "audit", "Audit")),
      "audit_record" -> (("audit", "auditRecord", "AuditRecord")),
      "feedback" -> (("feedback", "feedback", "Feedback")),
      "flyway_schema_history" -> (("migrate", "migration", "Migration")),
      "note" -> (("note", "note", "Note")),
      "oauth2_info" -> (("auth", "oAuth2Info", "OAuth2Info")),
      "password_info" -> (("auth", "passwordInfo", "PasswordInfo")),
      "scheduled_task_run" -> (("task", "scheduledTaskRun", "ScheduledTaskRun")),
      "system_permission" -> (("permission", "permission", "Permission")),
      "system_user" -> (("user", "systemUser", "SystemUser"))
    )
  }
}

class ProjectLoadService(p: ProjectileService) {
  def load(cfg: ConfigService, key: String) = {
    val summary = p.getProjectSummary(key)
    val input = p.getInput(summary.input)
    transform(cfg.projectDirectory, summary, input)
  }

  def transform(dir: File, summary: ProjectSummary, input: Input) = {
    EnumFeature.values.toString() // Weird Enumeratum classloading problem
    val customEnums = loadDir[EnumMember](dir, s"${summary.key}/enum")
    val enums = (customEnums.map(_.key) ++ input.enums.map(_.key)).distinct.sorted.map { key =>
      customEnums.find(_.key == key).getOrElse {
        input.enums.find(_.key == key) match {
          case Some(ie) => EnumMember(pkg = ie.pkg, key = key, features = summary.defaultEnumFeatures.map(EnumFeature.withValue))
          case None => throw new IllegalStateException("Inconceivable!")
        }
      }
    }

    val customModels = loadDir[ModelMember](dir, s"${summary.key}/model")
    val models = (customModels.map(_.key) ++ input.models.map(_.key)).distinct.sorted.map(key => customModels.find(_.key == key).getOrElse {
      input.models.find(_.key == key) match {
        case Some(im) => ProjectLoadService.Provided.models.get(key) match {
          case Some(opts) =>
            val pkg = summary.packages.getOrElse(OutputPackage.System, OutputPackage.System.defaultVal)
            ModelMember(pkg = pkg :+ opts._1, key = key, features = Set.empty, overrides = Seq(
              MemberOverride(k = "propertyName", v = opts._2), MemberOverride(k = "className", v = opts._3), MemberOverride(k = "provided", v = "true")
            ))
          case None => ModelMember(pkg = im.pkg, key = key, features = summary.defaultModelFeatures.map(ModelFeature.withValue))
        }
        case None => throw new IllegalStateException("Inconceivable!")
      }
    })

    val customUnions = loadDir[UnionMember](dir, s"${summary.key}/union")
    val unions = (customUnions.map(_.key) ++ input.unions.map(_.key)).distinct.sorted.map(key => customUnions.find(_.key == key).getOrElse {
      input.unions.find(_.key == key) match {
        case Some(ie) => UnionMember(pkg = ie.pkg, key = key)
        case None => throw new IllegalStateException("Inconceivable!")
      }
    })

    val customServices = loadDir[ServiceMember](dir, s"${summary.key}/service")
    val services = (customServices.map(_.key) ++ input.services.map(_.key)).distinct.sorted.map(key => customServices.find(_.key == key).getOrElse {
      input.services.find(_.key == key) match {
        case Some(ie) => ServiceMember(pkg = ie.pkg, key = key, features = summary.defaultServiceFeatures.map(ServiceFeature.withValue))
        case None => throw new IllegalStateException("Inconceivable!")
      }
    })

    val project = summary.into[Project]
      .withFieldComputed(_.enums, _ => enums)
      .withFieldComputed(_.models, _ => models)
      .withFieldComputed(_.unions, _ => unions)
      .withFieldComputed(_.services, _ => services)
      .transform

    project.setInput(input)
    project
  }

  private[this] def loadDir[A: Decoder](dir: File, k: String) = {
    val d = dir / k
    if (d.exists && d.isDirectory && d.isReadable) {
      d.children.filter(f => f.isRegularFile && f.name.endsWith(".json")).map(f => JsonFileLoader.loadFile[A](f, k)).toList
    } else {
      Nil
    }
  }
}
