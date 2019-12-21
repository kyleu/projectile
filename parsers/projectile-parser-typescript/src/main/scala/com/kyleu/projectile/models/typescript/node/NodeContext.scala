package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.typescript.jsdoc.JsDocNode
import com.kyleu.projectile.util.JsonSerializers._
import io.circe.Json

object NodeContext {
  def shouldIncludeJson(x: SyntaxKind) = x match {
    case SyntaxKind.SourceFile => false
    case _ => true
  }

  implicit val jsonEncoder: Encoder[NodeContext] = deriveEncoder
  implicit val jsonDecoder: Decoder[NodeContext] = deriveDecoder

  val empty = NodeContext()
}

final case class NodeContext(
    pos: Int = 0,
    end: Int = 0,
    kind: SyntaxKind = SyntaxKind.Unknown,
    jsDoc: Option[JsDocNode] = None,
    flags: Seq[NodeFlag] = Nil,
    modifiers: Set[ModifierFlag] = Set.empty,
    keys: Seq[String] = Nil,
    src: Seq[String] = Seq("-Empty NodeContext-"),
    json: Json = Json.fromInt(0)
) {
  def isAbstract = modifiers(ModifierFlag.Abstract)
  def isConst = modifiers(ModifierFlag.Const) || modifiers(ModifierFlag.Readonly) || flags.contains(NodeFlag.Let) || flags.contains(NodeFlag.Const)
  def isPublic = modifiers.forall(x => x != ModifierFlag.Protected && x != ModifierFlag.Private)
  def isPrivate = modifiers(ModifierFlag.Private)

  override def toString = s"[$pos-$end]: $kind [${keys.mkString(", ")}]"

  def plusFlags(ctx: NodeContext) = copy(flags = flags ++ ctx.flags, modifiers = modifiers ++ ctx.modifiers)
}
