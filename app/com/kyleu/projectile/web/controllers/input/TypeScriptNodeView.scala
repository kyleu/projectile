package com.kyleu.projectile.web.controllers.input

import com.kyleu.projectile.models.typescript.{TypeScriptNode, TypeScriptNodeHelper}
import play.twirl.api.Html

object TypeScriptNodeView {
  def view(node: TypeScriptNode, depth: Int) = {
    com.kyleu.projectile.web.views.html.input.ts.nodeComponent(node, Html(TypeScriptNodeHelper.asString(node)), depth)
  }
}
