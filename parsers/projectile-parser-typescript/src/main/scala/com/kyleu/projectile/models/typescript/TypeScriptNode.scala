package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.util.JsonSerializers._

sealed trait TypeScriptNode

object TypeScriptNode {
  implicit val jsonEncoder: Encoder[TypeScriptNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeScriptNode] = deriveDecoder

  case class SourceFile(filename: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode

  case class Comment(content: String, ctx: NodeContext) extends TypeScriptNode

  case class InterfaceDecl(name: String, members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode
  case class ModuleDecl(name: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode
  case class ClassDecl(name: String, ctx: NodeContext) extends TypeScriptNode
  case class PropertySig(name: String, ctx: NodeContext) extends TypeScriptNode
  case class MethodSig(name: String, ctx: NodeContext) extends TypeScriptNode
  case class VariableStatement(declarations: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode

  case class Unknown(kind: String, ctx: NodeContext) extends TypeScriptNode
  case class Error(kind: String, cls: String, msg: String, json: String, ctx: NodeContext) extends TypeScriptNode
}
