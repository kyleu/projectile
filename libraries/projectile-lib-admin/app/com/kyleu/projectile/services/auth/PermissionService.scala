package com.kyleu.projectile.services.auth

import com.kyleu.projectile.models.auth.Permission
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

object PermissionService extends Logging {
  case class ModelInfo(key: String, title: String, icon: Option[String], actions: Seq[String])
  case class PackageInfo(key: String, title: String, icon: String, models: Seq[ModelInfo] = Nil)

  def initialize(perms: Seq[Permission]) = {
    registerRole("admin", "Administrator", "A superuser that can do anything")
    registerRole("user", "User", "A normal system user")

    services.clear()
    perms.groupBy(_.role).foreach { case (role, p) => services(role) = PermissionService(p.toSet) }
    log.info(s"Permissions service loaded with [${perms.size}] database rows")(TraceData.noop)
  }

  def check(role: String, pkg: String, model: String, action: String) = services.get(role) match {
    case Some(svc) => svc.check(pkg, model, action)
    case None => (role == "admin") -> "uninitialized"
  }

  private[this] val services = collection.mutable.HashMap.empty[String, PermissionService]

  private[this] val roleInfo = collection.mutable.HashMap.empty[String, (String, String)]
  def roles() = roleInfo.toSeq.sortBy(_._1)
  def registerRole(key: String, title: String, description: String) = roleInfo(key) = title -> description

  private[this] val packageInfo = collection.mutable.HashMap.empty[String, PackageInfo]
  def packages() = packageInfo.values.toSeq.sortBy(_.key)
  def registerPackage(key: String, title: String, icon: String) = packageInfo(key) = packageInfo.get(key) match {
    case Some(p) => p.copy(title = title, icon = icon)
    case None => PackageInfo(key, title, icon, Nil)
  }
  def registerModel(pkg: String, key: String, title: String, icon: Option[String], actions: String*) = {
    val p = getPackage(pkg, key)
    p.models.find(_.key == key) match {
      case Some(m) => packageInfo(pkg) = p.copy(models = p.models.map {
        case mod if mod.key == key => m.copy(title = title, icon = icon, actions = m.actions.filterNot(a => actions.contains(a)) ++ actions)
        case x => x
      })
      case None => packageInfo(pkg) = p.copy(models = p.models :+ ModelInfo(key = key, title = title, icon = icon, actions = actions))
    }
  }
  def registerActions(pkg: String, model: String, actions: String*) = {
    val p = getPackage(pkg, model)
    p.models.find(_.key == model) match {
      case None => throw new IllegalStateException(s"Model [$model] in package [$pkg] hasn't been registered with the permission service")
      case Some(m) => packageInfo(pkg) = p.copy(models = p.models.map {
        case mod if mod.key == model => m.copy(actions = m.actions.filterNot(a => actions.contains(a)) ++ actions)
        case x => x
      })
    }
  }

  private[this] def getPackage(key: String, model: String) = packageInfo.getOrElseUpdate(key, PackageInfo(key, key, key))
}

case class PermissionService(perms: Set[Permission]) {
  val keys = perms.map(p => (p.pkg, p.model, p.action) -> p).toMap
  def check(pkg: String, model: String, action: String) = {
    keys.get((Some(pkg), Some(model), Some(action))).map(_ -> "action").orElse(keys.get((Some(pkg), Some(model), None)).map(_ -> "model")).orElse {
      keys.get((Some(pkg), None, None)).map(_ -> "package").orElse(keys.get((None, None, None)).map(_ -> "global"))
    }.map(x => x._1.allow -> x._2).getOrElse(false -> "unset")
  }
}
