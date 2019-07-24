package com.kyleu.projectile.models.module

import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}

sealed abstract class ApplicationFeature(override val value: String) extends StringEnumEntry {
  def onEnabled(): Unit = {}
}

object ApplicationFeature extends StringEnum[ApplicationFeature] with StringCirceEnum[ApplicationFeature] {
  PermissionService.registerPackage("tools", "System Tools", InternalIcons.tools)
  PermissionService.registerPackage("models", "System Models", InternalIcons.models)

  case object Analytics extends ApplicationFeature("analytics")
  case object Audit extends ApplicationFeature("audit")
  case object Connection extends ApplicationFeature("connection")
  case object Encryption extends ApplicationFeature("encryption")
  case object Feedback extends ApplicationFeature("feedback")
  case object Graphql extends ApplicationFeature("graphql")
  case object Help extends ApplicationFeature("help")
  case object Migrate extends ApplicationFeature("migrate")
  case object Note extends ApplicationFeature("note")
  case object Permission extends ApplicationFeature("permission")
  case object Process extends ApplicationFeature("process")
  case object Profile extends ApplicationFeature("profile")
  case object Reporting extends ApplicationFeature("reporting")
  case object Rest extends ApplicationFeature("rest")
  case object Sandbox extends ApplicationFeature("sandbox")
  case object Search extends ApplicationFeature("search")
  case object Sitemap extends ApplicationFeature("sitemap")
  case object Sql extends ApplicationFeature("sql")
  case object Status extends ApplicationFeature("status")
  case object Task extends ApplicationFeature("task")
  case object User extends ApplicationFeature("user")

  private[this] var enabledFeatures = Set.empty[ApplicationFeature]

  def enabled = enabledFeatures

  def enableKey(key: String) = enable(ApplicationFeature.withValue(key))

  def enable(feature: ApplicationFeature) = enabledFeatures = enabledFeatures + feature

  override val values = findValues
}
