package com.kyleu.projectile.services.status

import java.time.LocalDate

object AppVersions {
  case class Version(on: LocalDate, v: String, title: String, desc: Option[String], features: Seq[(String, String, () => Option[String])])

  private[this] var versions = List.empty[Version]
  def getVersions = versions

  def register(on: String, v: String, title: String, desc: Option[String] = None, features: Seq[(String, String, () => Option[String])] = Nil) = {
    versions = versions.filterNot(_.v == v) :+ Version(LocalDate.parse(on), v, title, desc, features)
  }
}
