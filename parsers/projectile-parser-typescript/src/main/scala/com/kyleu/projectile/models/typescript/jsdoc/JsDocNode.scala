package com.kyleu.projectile.models.typescript.jsdoc

import com.kyleu.projectile.util.JsonSerializers._

object JsDocNode {
  object Parameter {
    implicit val jsonEncoder: Encoder[Parameter] = deriveEncoder
    implicit val jsonDecoder: Decoder[Parameter] = deriveDecoder
  }

  case class Parameter(name: String, comment: Option[String])

  implicit val jsonEncoder: Encoder[JsDocNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[JsDocNode] = deriveDecoder
}

case class JsDocNode(
    comment: Option[String],
    params: Seq[JsDocNode.Parameter] = Nil,
    ret: Option[String] = None,
    since: Option[String] = None,
    version: Option[String] = None,
    deprecated: Option[String] = None,
    interface: Option[String] = None,
    examples: Seq[String] = Nil,
    defaults: Seq[String] = Nil,
    ext: Seq[String] = Nil,
    see: Seq[String] = Nil,
    aliases: Seq[String] = Nil,
    tags: Set[String] = Set.empty,
    unprocessed: Seq[Json] = Nil
) {
  lazy val commentSeq = comment.map(_.split("\n").toList).getOrElse(Nil).dropWhile(_.trim.isEmpty).reverse.dropWhile(_.trim.isEmpty).reverse
}
