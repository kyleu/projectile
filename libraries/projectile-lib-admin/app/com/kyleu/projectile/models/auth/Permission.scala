package com.kyleu.projectile.models.auth

import java.time.LocalDateTime
import java.util.UUID

import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object Permission {
  object Path {
    implicit val jsonEncoder: Encoder[Path] = deriveEncoder
    implicit val jsonDecoder: Decoder[Path] = deriveDecoder
  }

  case class Path(pkg: String, model: String, act: String)
}

case class Permission(
    role: String,
    pkg: Option[String],
    model: Option[String],
    action: Option[String],
    allow: Boolean,
    created: LocalDateTime = DateUtils.now,
    createdBy: Option[UUID]
) {
  def key = pkg.map(p => s"package [$p]").getOrElse("all packages") + ", " +
    model.map(m => s"model [$m]").getOrElse("all models") + ", " +
    action.map(a => s"action [$a]").getOrElse("all actions")
}
