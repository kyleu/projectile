package com.kyleu.projectile.models.typescript

import com.kyleu.projectile.models.typescript.TypeScriptNode._

object TypeScriptNodeHelper {
  def asString(n: TypeScriptNode) = n match {
    case node: ClassDecl => "class " + node.name
    case node: Constructor => s"constructor(${node.params.map(p => p.k + ": " + p.t).mkString(", ")}"
    case node: ExportAssignment => "-export-"
    case node: InterfaceDecl => "interface " + node.name
    case node: MethodDecl => s"${node.name}(${node.params.map(p => p.k + ": " + p.t).mkString(", ")}): ${node.ret}"
    case node: MethodSig => node.name
    case node: ModuleDecl => "module " + node.name
    case node: PropertySig => node.name + ": " + node.typ
    case node: SourceFile => node.filename
    case node: Unknown => node.kind
    case node: VariableStmt => "-variable-"
    case _ => "TODO: " + n.toString
  }

  def getChildren(n: TypeScriptNode): Seq[TypeScriptNode] = n match {
    case node: InterfaceDecl => node.members
    case node: SourceFile => node.statements
    case _ => Nil
  }
}
