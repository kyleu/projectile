package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.export.typ.{FieldType, ObjectField}
import com.kyleu.projectile.util.JsonSerializers._

sealed abstract class TypeScriptNode() {
  def ctx: NodeContext
}

object TypeScriptNode {
  implicit val jsonEncoder: Encoder[TypeScriptNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeScriptNode] = deriveDecoder

  case class SourceFile(filename: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode

  // case class Comment(content: String, ctx: NodeContext) extends TypeScriptNode

  case class InterfaceDecl(name: String, members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode
  case class ModuleDecl(name: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode
  case class ClassDecl(name: String, members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode
  case class MethodDecl(name: String, params: Seq[ObjectField], ret: FieldType, ctx: NodeContext) extends TypeScriptNode

  case class Constructor(ctx: NodeContext, params: Seq[ObjectField]) extends TypeScriptNode
  case class PropertySig(name: String, typ: FieldType, ctx: NodeContext) extends TypeScriptNode
  case class MethodSig(name: String, ctx: NodeContext) extends TypeScriptNode

  case class ExportAssignment(ctx: NodeContext) extends TypeScriptNode
  case class VariableStmt(declarations: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode

  case class Unknown(kind: String, ctx: NodeContext) extends TypeScriptNode
  case class Error(kind: String, cls: String, msg: String, json: Json, ctx: NodeContext) extends TypeScriptNode
}
