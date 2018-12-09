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
    inputs.flatMap { i =>
      val enums = i.exportEnums.filterNot(ek => p.enums.exists(_.key == ek.key))
      val models = i.exportModels.filterNot(mk => p.models.exists(_.key == mk.key))
      val services = i.exportServices.filterNot(sk => p.services.exists(_.key == sk.key))

      enums.foreach(e => svc.saveEnumMember(p.key, EnumMember(input = i.key, pkg = e.pkg, key = e.key, features = p.enumFeatures.toSet)))
      models.foreach(m => svc.saveModelMember(p.key, ModelMember(input = i.key, pkg = m.pkg, key = m.key, features = p.modelFeatures.toSet)))
      services.foreach(s => svc.saveServiceMember(p.key, ServiceMember(input = i.key, pkg = s.pkg, key = s.key, features = p.serviceFeatures.toSet)))

      Some(s"Updated input [${i.key}], adding [${enums.size}] enums, [${models.size}] models, and [${services.size}] services.")
    }
  }
}
