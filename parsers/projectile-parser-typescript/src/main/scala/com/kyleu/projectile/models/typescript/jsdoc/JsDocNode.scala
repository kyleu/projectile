package com.kyleu.projectile.models.typescript.jsdoc

import com.kyleu.projectile.util.JsonSerializers._

object JsDocNode {
  object Parameter {
    implicit val jsonEncoder: Encoder[Parameter] = deriveEncoder
    implicit val jsonDecoder: Decoder[Parameter] = deriveDecoder
  }

  final case class Parameter(name: String, comment: Seq[String])

  implicit val jsonEncoder: Encoder[JsDocNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[JsDocNode] = deriveDecoder

  val empty = JsDocNode()
}

final case class JsDocNode(
    comment: Seq[String] = Nil,
    params: Seq[JsDocNode.Parameter] = Nil,
    ret: Seq[String] = Nil,
    since: Seq[String] = Nil,
    version: Seq[String] = Nil,
    deprecated: Seq[String] = Nil,
    interface: Seq[String] = Nil,
    examples: Seq[Seq[String]] = Nil,
    defaults: Seq[Seq[String]] = Nil,
    ext: Seq[String] = Nil,
    see: Seq[Seq[String]] = Nil,
    aliases: Seq[Seq[String]] = Nil,
    tags: Set[String] = Set.empty,
    authors: Seq[String] = Nil,
    unprocessed: Seq[Json] = Nil
) {
  def emptyOpt = if (this == JsDocNode.empty) { None } else { Some(this) }

  lazy val singleLine = comment.size == 1 && deprecated.size < 2 && emptyMembers
  lazy val isEmpty = comment.isEmpty && deprecated.size < 2 && emptyMembers

  private[this] lazy val emptyMembers = params.isEmpty && ret.isEmpty && since.isEmpty && version.isEmpty && interface.isEmpty &&
    examples.isEmpty && defaults.isEmpty && ext.isEmpty && see.isEmpty && aliases.isEmpty && tags.isEmpty && authors.isEmpty && unprocessed.isEmpty

  def merge(o: JsDocNode) = JsDocNode(
    comment = comment ++ o.comment,
    params = params ++ o.params,
    ret = ret ++ o.ret,
    since = since ++ o.since,
    version = version ++ o.version,
    deprecated = deprecated ++ o.deprecated,
    interface = interface ++ o.interface,
    examples = examples ++ o.examples,
    defaults = defaults ++ o.defaults,
    ext = ext ++ o.ext,
    see = see ++ o.see,
    aliases = aliases ++ o.aliases,
    tags = tags ++ o.tags,
    unprocessed = unprocessed ++ o.unprocessed
  )
}
