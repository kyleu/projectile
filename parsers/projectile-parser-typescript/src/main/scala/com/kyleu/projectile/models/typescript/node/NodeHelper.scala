package com.kyleu.projectile.models.typescript.node

import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeRequired, TypeParam}
import com.kyleu.projectile.models.typescript.node.TypeScriptNode._
import com.kyleu.projectile.models.typescript.output.OutputHelper._

object NodeHelper {
  def asString(n: TypeScriptNode): String = n match {
    case node: SourceFile => "src:" + node.path
    case node: SourceFileReference => "file:" + node.path
    case node: TypesReference => "types:" + node.ref

    case _: ImportDecl => "import ???"
    case _: ExportDecl => "export ???"
    case node: ExportNamespaceDecl => "export as namespace " + node.name
    case node: InterfaceDecl => "interface " + node.name
    case node: ModuleDecl => "module " + node.name
    case node: ClassDecl => "class " + node.name + ptp(node.tParams)
    case node: MethodDecl => s"${keywords(node)}${node.name}${ptp(node.tParams)}(${node.params.mkString(", ")}): ${ptr(node.ret)}"
    case node: VariableDecl => "var " + node.name + ": " + ptr(node.typ)
    case node: TypeAliasDecl => "alias " + node.name + " = " + pt(node.typ)
    case node: PropertyDecl => keywords(node) + node.name + ": " + ptr(node.typ)
    case node: EnumDecl => "enum " + node.name

    case _: ModuleBlock => "-module block-"

    case node: Constructor => s"constructor(${node.params.mkString(", ")})"
    case node: ConstructSig => s"constructor(${node.params.mkString(", ")}): ${pt(node.typ)}"
    case node: IndexSig => s"[${node.params.mkString(", ")}]: ${pt(node.typ)}"
    case node: PropertySig => node.name + ": " + ptr(node.typ)
    case node: CallSig => s"(${node.params.mkString(", ")}): ${ptr(node.ret)}"
    case node: MethodSig => s"${node.name}${ptp(node.tParams)}(${node.params.mkString(", ")}): ${ptr(node.ret)}"

    case node: ExportAssignment => s"export = " + node.exp
    case _: VariableStmt => "-variable-"

    case node: EnumMember => node.name + node.initial.map(" = " + _).getOrElse("")

    case node: LogMessage => "log[" + node.msg + "]"
    case node: Unknown => node.kind
    case node: Error => s"error(${node.kind}: ${node.msg})"
  }

  def getModuleReferenceNodes(n: TypeScriptNode): Seq[TypesReference] = {
    Seq(n).collect { case s: TypesReference => s } ++ n.children.flatMap(getModuleReferenceNodes)
  }
  def getSourceFileReferenceNodes(n: TypeScriptNode): Seq[SourceFileReference] = {
    Seq(n).collect { case s: SourceFileReference => s } ++ n.children.flatMap(getSourceFileReferenceNodes)
  }
  def getSourceFileNodes(n: TypeScriptNode): Seq[SourceFile] = {
    Seq(n).collect { case s: SourceFile => s } ++ n.children.flatMap(getSourceFileNodes)
  }
  def getUnknownNodes(n: TypeScriptNode): Seq[Unknown] = {
    Seq(n).collect { case u: Unknown => u } ++ n.children.flatMap(getUnknownNodes)
  }
  def getErrorNodes(n: TypeScriptNode): Seq[Error] = {
    Seq(n).collect { case e: Error => e } ++ n.children.flatMap(getErrorNodes)
  }

  def pt(t: FieldType) = t match {
    case FieldType.StructType(key, _) => key
    case _ => t.toString
  }

  private[this] def ptr(t: FieldTypeRequired) = pt(t.t) + (if (t.r) { "" } else { "?" })
  private[this] def ptp(tParams: Seq[TypeParam]) = if (tParams.isEmpty) { "" } else { "[" + tParams.mkString(", ") + "]" }
}
