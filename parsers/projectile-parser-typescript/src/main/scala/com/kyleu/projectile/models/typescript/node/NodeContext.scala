package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.typescript.jsdoc.JsDocNode
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.Json

object NodeContext {
  def shouldIncludeJson(x: SyntaxKind) = x match {
    case SyntaxKind.SourceFile => false
    case _ => false
  }

  implicit val jsonEncoder: Encoder[NodeContext] = deriveEncoder
  implicit val jsonDecoder: Decoder[NodeContext] = deriveDecoder

  val empty = NodeContext(0, 0, SyntaxKind.Unknown, Nil, Nil, Nil, Seq("-Empty NodeContext-"), Json.fromInt(0))
}

case class NodeContext(pos: Int, end: Int, kind: SyntaxKind, jsDoc: Seq[JsDocNode], flags: Seq[NodeFlag], keys: Seq[String], src: Seq[String], json: Json) {
  override def toString = s"[$pos-$end]: $kind [${keys.mkString(", ")}]"
}
