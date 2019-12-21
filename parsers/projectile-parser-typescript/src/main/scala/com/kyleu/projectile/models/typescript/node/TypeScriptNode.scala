package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeRequired, ObjectField, TypeParam}
import com.kyleu.projectile.util.JsonSerializers._

sealed abstract class TypeScriptNode(val nameOpt: Option[String] = None, val types: Seq[FieldType] = Nil, val children: Seq[TypeScriptNode] = Nil) {
  def ctx: NodeContext
}

object TypeScriptNode {
  implicit val jsonEncoder: Encoder[TypeScriptNode] = deriveEncoder
  implicit val jsonDecoder: Decoder[TypeScriptNode] = deriveDecoder

  final case class SourceFile(
      path: String, header: SourceFileHeader, statements: Seq[TypeScriptNode], ctx: NodeContext
  ) extends TypeScriptNode(children = statements)
  final case class SourceFileReference(path: String, ctx: NodeContext) extends TypeScriptNode
  final case class TypesReference(ref: String, ctx: NodeContext) extends TypeScriptNode

  final case class ImportDecl(ctx: NodeContext) extends TypeScriptNode

  final case class ExportDecl(ctx: NodeContext) extends TypeScriptNode

  final case class ExportNamespaceDecl(name: String, ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name))

  final case class InterfaceDecl(
      name: String, tParams: Seq[TypeParam], members: Seq[TypeScriptNode], ctx: NodeContext
  ) extends TypeScriptNode(nameOpt = Some(name), types = tParams.flatMap(_.types), children = members)

  final case class ModuleDecl(name: String, statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), children = statements)

  final case class ClassDecl(
      name: String, tParams: Seq[TypeParam], members: Seq[TypeScriptNode], ctx: NodeContext
  ) extends TypeScriptNode(nameOpt = Some(name), types = tParams.flatMap(_.types), children = members)

  final case class MethodDecl(
      name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext
  ) extends TypeScriptNode(nameOpt = Some(name), types = tParams.flatMap(_.types) ++ params.map(_.t) :+ ret.t)

  final case class VariableDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), types = Seq(typ.t))

  final case class TypeAliasDecl(name: String, typ: FieldType, ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), types = Seq(typ))

  final case class PropertyDecl(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), types = Seq(typ.t))

  final case class EnumDecl(name: String, members: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), children = members)

  final case class ModuleBlock(statements: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(children = statements)

  final case class Constructor(params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode(types = params.map(_.t))

  final case class ConstructSig(typ: FieldType, params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode(types = params.map(_.t) :+ typ)

  final case class IndexSig(typ: FieldType, params: Seq[ObjectField], ctx: NodeContext) extends TypeScriptNode(types = params.map(_.t) :+ typ)

  final case class PropertySig(name: String, typ: FieldTypeRequired, ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name), types = Seq(typ.t))

  final case class CallSig(
      tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext
  ) extends TypeScriptNode(types = tParams.flatMap(_.types) ++ params.map(_.t) :+ ret.t)

  final case class MethodSig(
      name: String, tParams: Seq[TypeParam], params: Seq[ObjectField], ret: FieldTypeRequired, ctx: NodeContext
  ) extends TypeScriptNode(nameOpt = Some(name), types = tParams.flatMap(_.types) ++ params.map(_.t) :+ ret.t)

  final case class ExportAssignment(exp: String, ctx: NodeContext) extends TypeScriptNode

  final case class VariableStmt(declarations: Seq[TypeScriptNode], ctx: NodeContext) extends TypeScriptNode(children = declarations)

  final case class EnumMember(name: String, initial: Option[Json], ctx: NodeContext) extends TypeScriptNode(nameOpt = Some(name))

  final case class LogMessage(msg: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode

  final case class Unknown(kind: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode

  final case class Error(kind: String, cls: String, msg: String, json: Json = io.circe.Json.Null, ctx: NodeContext = NodeContext.empty) extends TypeScriptNode
}
