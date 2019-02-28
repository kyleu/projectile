package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.util.JsonSerializers._

case class NodeContext(pos: Int, end: Int, kind: SyntaxKind, jsDoc: Seq[String], flags: Seq[NodeFlag], keys: Seq[String]) {
  override def toString = s"[$pos-$end]: $kind [${keys.mkString(", ")}]"
}

object NodeContext {
  implicit val jsonEncoder: Encoder[NodeContext] = deriveEncoder
  implicit val jsonDecoder: Decoder[NodeContext] = deriveDecoder
}
