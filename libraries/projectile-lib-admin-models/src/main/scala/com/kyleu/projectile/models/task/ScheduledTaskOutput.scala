package com.kyleu.projectile.models.task

import java.time.LocalDateTime

import com.kyleu.projectile.util.DateUtils
import com.kyleu.projectile.util.JsonSerializers._

object ScheduledTaskOutput {
  implicit val jsonEncoder: Encoder[ScheduledTaskOutput] = deriveEncoder
  implicit val jsonDecoder: Decoder[ScheduledTaskOutput] = deriveDecoder

  final case class Log(msg: String, offset: Int)

  object Log {
    implicit val jsonEncoder: Encoder[ScheduledTaskOutput.Log] = deriveEncoder
    implicit val jsonDecoder: Decoder[ScheduledTaskOutput.Log] = deriveDecoder
  }
}

final case class ScheduledTaskOutput(
    userId: String,
    username: String,
    status: String,
    logs: Seq[ScheduledTaskOutput.Log],
    start: LocalDateTime,
    end: Option[LocalDateTime]
) {
  val elapsedMs = (DateUtils.toMillis(end.getOrElse(DateUtils.now)) - DateUtils.toMillis(start)).toInt
}
