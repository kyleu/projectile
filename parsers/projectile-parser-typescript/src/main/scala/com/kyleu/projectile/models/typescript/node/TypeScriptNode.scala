package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeRequired, ObjectField, TypeParam}
import com.kyleu.projectile.util.JsonSerializers._

sealed abstract class TypeScriptNode(val children: Seq[TypeScriptNode] = Nil) {
  def ctx: NodeContext
}

object TypeScriptNode {
  implicit val jsonEncoder: Encoder[TypeScriptNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeScriptNode] = deriveDecoder

  case class SourceFile(path: String, header: SourceFileHeader, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(statements)
  case class SourceFileReference(path: String, ctx: NodeContext) extends TypeScriptNode

  case class ImportDecl(ctx: NodeContext) extends TypeScriptNode
  case class ExportDecl(ctx: NodeContext) extends TypeScriptNode
  case class ExportNamespaceDecl(name: String, ctx: NodeContext) extends TypeScriptNode
  case class InterfaceDecl(name: String, tParams: Seq[TypeParam], members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(members)
  case class ModuleDecl(name: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(statements)
  case class ClassDecl(name: String, tParams: Seq[TypeParam], members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(members)
  case class MethodDecl(name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode
  case class VariableDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode
  case class TypeAliasDecl(name: String, typ: FieldType, ctx: NodeContext) extends TypeScriptNode
  case class PropertyDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode
  case class EnumDecl(name: String, members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(members)

  case class ModuleBlock(statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(statements)

  case class Constructor(params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode
  case class ConstructSig(typ: FieldType, params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode
  case class IndexSig(typ: FieldType, params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode
  case class PropertySig(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode
  case class CallSig(params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode
  case class MethodSig(name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode

  case class ExportAssignment(exp: String, ctx: NodeContext) extends TypeScriptNode
  case class VariableStmt(declarations: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(declarations)

  case class EnumMember(name: String, initial: Option[Json], ctx: NodeContext) extends TypeScriptNode

  case class LogMessage(msg: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode
  case class Unknown(kind: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode
  case class Error(kind: String, cls: String, msg: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode
}
