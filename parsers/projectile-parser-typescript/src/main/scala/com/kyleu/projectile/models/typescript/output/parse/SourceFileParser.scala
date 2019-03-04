package com.kyleu.projectile.models.typescript.output.parse

import better.files.File
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.MarkdownFile
import com.kyleu.projectile.models.typescript.node.TypeScriptNode.SourceFile

object SourceFileParser {
  def forSourceFiles(ctx: ParseContext, nodes: Seq[SourceFile]) = {
    val file = MarkdownFile(OutputPath.SharedSource, ctx.pkg, "index")
    file.add()
    file.add(s"The contents of this package were created by [Projectile](https://kyleu.com/projectile) from parsing:")
    nodes.foreach(node => file.add(s"  - ${ctx.root.relativize(File(node.path)).toString}"))

    val headers = nodes.flatMap(n => n.header)
    if (headers.nonEmpty) {
      file.add()
      file.add("```typescript")
      headers.foreach(file.add(_))
      file.add("```")
    }

    file
  }
}
