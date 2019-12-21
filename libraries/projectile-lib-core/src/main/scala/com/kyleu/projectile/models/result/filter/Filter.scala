package com.kyleu.projectile.models.result.filter

import com.kyleu.projectile.util.JsonSerializers._

object Filter {
  implicit val jsonEncoder: Encoder[Filter] = deriveEncoder
  implicit val jsonDecoder: Decoder[Filter] = deriveDecoder

  def strEq(k: String, v: String) = Filter(k = k, v = Seq(v))
}

final case class Filter(
    k: String = "?",
    o: FilterOp = FilterOp.Equal,
    v: Seq[String] = Nil
)
