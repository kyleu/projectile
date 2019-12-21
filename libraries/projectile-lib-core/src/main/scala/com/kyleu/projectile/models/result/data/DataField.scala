package com.kyleu.projectile.models.result.data

import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.NullUtils

object DataField {
  implicit val jsonEncoder: Encoder[DataField] = deriveEncoder
  implicit val jsonDecoder: Decoder[DataField] = deriveDecoder
}

final case class DataField(k: String, v: Option[String]) {
  val value = v.getOrElse(NullUtils.inst)
}
