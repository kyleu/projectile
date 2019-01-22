package com.kyleu.projectile.models.result.filter

import com.kyleu.projectile.util.JsonSerializers._

object Filter {
  implicit val jsonEncoder: Encoder[Filter] = deriveEncoder
  implicit val jsonDecoder: Decoder[Filter] = deriveDecoder
}

case class Filter(
    k: String = "?",
    o: FilterOp = FilterOp.Equal,
    v: Seq[String] = Nil
)
