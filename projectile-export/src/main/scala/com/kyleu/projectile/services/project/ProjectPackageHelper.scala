package com.kyleu.projectile.services.project

import com.kyleu.projectile.models.export.{ExportEnum, ExportModel, ExportService}
import com.kyleu.projectile.services.ProjectileService

trait ProjectPackageHelper { this: ProjectileService =>
  def togglePackage(key: String, item: String, pkg: String) = {
    val p = getProject(key)
    val newPkg = pkg.split(".").map(_.trim).filter(_.nonEmpty).toList
    val ret: Product = p.models.find(_.key == item) match {
      case Some(model) => saveModelMember(key, model.copy(pkg = newPkg))
      case None => p.enums.find(_.key == item) match {
        case Some(e) => saveEnumMember(key, e.copy(pkg = newPkg))
        case None => p.services.find(_.key == item) match {
          case Some(s) => saveServiceMember(key, s.copy(pkg = newPkg))
          case None => throw new IllegalStateException(s"Project [$key] doesn't contain a model, enum, or service named [$item]")
        }
      }
    }
    ret match {
      case x: ExportModel => s"Model [$item] set to package [$pkg]"
      case x: ExportEnum => s"Enum [$item] set to package [$pkg]"
      case x: ExportService => s"Service [$item] set to package [$pkg]"
    }
  }
}
