package com.kyleu.projectile.models.typescript.output.parse

import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.models.typescript.node.{NodeHelper, TypeScriptNode}
import com.kyleu.projectile.models.typescript.output.{OutputHelper, TypeScriptOutput}

object MemberParser {
  def print(ctx: ParseContext, out: TypeScriptOutput, tsn: TypeScriptNode, file: ScalaFile, last: Boolean = false) = {
    tsn match {
      case node =>
        OutputHelper.printContext(file, node.ctx)
        file.add(s"// ${NodeHelper.asString(node)}")
    }
    if (!last) { file.add() }
  }

}
