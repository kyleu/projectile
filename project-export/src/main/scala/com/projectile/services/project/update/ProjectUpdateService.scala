package com.projectile.services.project.update

import com.projectile.models.input.InputTemplate
import com.projectile.models.project.Project
import com.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}
import com.projectile.services.ProjectileService

object ProjectUpdateService {
  def update(svc: ProjectileService, p: Project) = {
    val inputs = (p.enums.map(_.input) ++ p.models.map(_.input) ++ p.services.map(_.input)).distinct.map(svc.getInput).filter(_.template match {
      case InputTemplate.Postgres => false
      case InputTemplate.Thrift => true
      case InputTemplate.GraphQL => true
      case InputTemplate.Filesystem => true
    })
    inputs.map { i =>
      val enums = i.exportEnums.filterNot(ek => p.enums.exists(_.key == ek.key))
      val models = i.exportModels.filterNot(mk => p.models.exists(_.key == mk.key))
      val services = i.exportServices.filterNot(sk => p.services.exists(_.key == sk.key))

      val enumsToRemove = p.enums.filterNot(ek => i.exportEnums.exists(_.key == ek.key))
      val modelsToRemove = p.models.filterNot(mk => i.exportModels.exists(_.key == mk.key))
      val servicesToRemove = p.services.filterNot(sk => i.exportServices.exists(_.key == sk.key))

      val ea = enums.map { e =>
        svc.saveEnumMember(p.key, EnumMember(input = i.key, pkg = e.pkg, key = e.key, features = p.enumFeatures.toSet))
        s"Added enum [${e.key}]"
      }
      val ma = models.map { m =>
        svc.saveModelMember(p.key, ModelMember(input = i.key, pkg = m.pkg, key = m.key, features = p.modelFeatures.toSet))
        s"Added model [${m.key}]"
      }
      val sa = services.map { s =>
        svc.saveServiceMember(p.key, ServiceMember(input = i.key, pkg = s.pkg, key = s.key, features = p.serviceFeatures.toSet))
        s"Added service [${s.key}]"
      }

      val er = enumsToRemove.map { e =>
        svc.removeEnumMember(p.key, e.key)
        s"Removed enum [${e.key}]"
      }
      val mr = modelsToRemove.map { m =>
        svc.removeModelMember(p.key, m.key)
        s"Removed model [${m.key}]"
      }
      val sr = servicesToRemove.map { s =>
        svc.removeServiceMember(p.key, s.key)
        s"Removed service [${s.key}]"
      }

      val results = ea ++ ma ++ sa ++ er ++ mr ++ sr
      val msg = if (results.isEmpty) {
        "No changes required"
      } else {
        results.mkString(", ")
      }
      s"Updated input [${i.key}]: $msg"
    }
  }
}
