package com.kyleu.projectile.models.status

import java.time.LocalDateTime

import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object AppStatus {
  object Timing {
    implicit val jsonEncoder: Encoder[Timing] = deriveEncoder
    implicit val jsonDecoder: Decoder[Timing] = deriveDecoder
  }

  case class Timing(msg: String, dur: Int)

  implicit val jsonEncoder: Encoder[AppStatus] = deriveEncoder
  implicit val jsonDecoder: Decoder[AppStatus] = deriveDecoder
}

case class AppStatus(
    name: String,
    version: String = "0.0.0",
    status: String = "OK",
    timings: Seq[AppStatus.Timing] = Nil,
    errors: Seq[String] = Nil,
    measured: LocalDateTime = DateUtils.now
)
