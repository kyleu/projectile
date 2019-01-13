package com.kyleu.projectile.services.project

import com.kyleu.projectile.models.feature.{EnumFeature, ModelFeature, ServiceFeature}
import com.kyleu.projectile.models.input.{Input, InputTemplate}
import com.kyleu.projectile.models.project.Project
import com.kyleu.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}
import com.kyleu.projectile.services.ProjectileService

object ProjectUpdateService {
  private[this] val updateDatabase = true
  private[this] val saveUnchanged = false

  def update(svc: ProjectileService, p: Project) = updateInput(svc, p, svc.getInput(p.input))

  def updateInput(svc: ProjectileService, p: Project, i: Input) = {
    val i = svc.getInput(p.input)
    if ((!updateDatabase) && i.template == InputTemplate.Postgres) {
      Seq(s"Skipping update of database input [${i.key}]")
    } else {
      doUpdate(svc, p, i)
    }
  }

  private[this] def doUpdate(svc: ProjectileService, p: Project, i: Input) = {
    val enumResults = processEnums(svc, p, i)
    val modelResults = processModels(svc, p, i)
    val serviceResults = processServices(svc, p, i)

    val results = enumResults ++ modelResults ++ serviceResults
    if (results.isEmpty) {
      Nil
    } else {
      val hashCode = svc.getInput(i.key).hashCode()
      svc.setProjectHash(p.key, hashCode)
      s"Updated project [${p.key}] with hashcode [$hashCode]:" +: results
    }
  }

  private[this] def processEnums(svc: ProjectileService, p: Project, i: Input) = {
    val (unchanged, enumsToAdd) = i.exportEnums.partition(ek => p.enums.exists(_.key == ek.key))
    val enumsToRemove = p.enums.filterNot(ek => i.exportEnums.exists(_.key == ek.key))
    val ef = p.defaultEnumFeatures.map(EnumFeature.withValue)

    if (saveUnchanged) { unchanged.map(e => svc.saveEnumMember(p.key, p.getEnum(e.key))) }

    enumsToAdd.map { e =>
      svc.saveEnumMember(p.key, EnumMember(pkg = e.pkg, key = e.key, features = ef))
      s"Added enum [${e.key}]"
    } ++ enumsToRemove.map { e =>
      svc.removeEnumMember(p.key, e.key)
      s"Removed enum [${e.key}]"
    }
  }

  private[this] def processModels(svc: ProjectileService, p: Project, i: Input) = {
    val (unchanged, modelsToAdd) = i.exportModels.partition(ek => p.models.exists(_.key == ek.key))
    val modelsToRemove = p.models.filterNot(mk => i.exportModels.exists(_.key == mk.key))
    val mf = p.defaultModelFeatures.map(ModelFeature.withValue)

    if (saveUnchanged) { unchanged.map(m => svc.saveModelMember(p.key, p.getModel(m.key))) }

    modelsToAdd.map { m =>
      svc.saveModelMember(p.key, ModelMember(pkg = m.pkg, key = m.key, features = mf))
      s"Added model [${m.key}]"
    } ++ modelsToRemove.map { m =>
      svc.removeModelMember(p.key, m.key)
      s"Removed model [${m.key}]"
    }
  }

  private[this] def processServices(svc: ProjectileService, p: Project, i: Input) = {
    val (unchanged, servicesToAdd) = i.exportServices.partition(sk => p.services.exists(_.key == sk.key))
    val servicesToRemove = p.services.filterNot(sk => i.exportServices.exists(_.key == sk.key))
    val sf = p.defaultServiceFeatures.map(ServiceFeature.withValue)

    if (saveUnchanged) { unchanged.map(s => svc.saveServiceMember(p.key, p.getService(s.key))) }

    servicesToAdd.map { s =>
      svc.saveServiceMember(p.key, ServiceMember(pkg = s.pkg, key = s.key, features = sf))
      s"Added service [${s.key}]"
    } ++ servicesToRemove.map { s =>
      svc.removeServiceMember(p.key, s.key)
      s"Removed service [${s.key}]"
    }
  }
}
