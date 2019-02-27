package com.kyleu.projectile.web.controllers.input

import com.kyleu.projectile.models.typescript.TypeScriptNode
import com.kyleu.projectile.models.typescript.TypeScriptNode._

object TypeScriptNodeView {
  def view(node: TypeScriptNode, depth: Int) = node match {
    case x: SourceFile => com.kyleu.projectile.web.views.html.input.ts.sourceFile(node = x, depth = depth)
    case x: ModuleDecl => com.kyleu.projectile.web.views.html.input.ts.moduleDecl(node = x, depth = depth)
    case x: InterfaceDecl => com.kyleu.projectile.web.views.html.input.ts.interfaceDecl(node = x, depth = depth)
    case x: PropertySig => com.kyleu.projectile.web.views.html.input.ts.propertySig(node = x, depth = depth)
    case x: MethodSig => com.kyleu.projectile.web.views.html.input.ts.methodSig(node = x, depth = depth)
    case x: Unknown => com.kyleu.projectile.web.views.html.input.ts.unknown(node = x, depth = depth)
    case _ => com.kyleu.projectile.web.views.html.input.ts.simpleNode(node, depth = depth)
  }
}
