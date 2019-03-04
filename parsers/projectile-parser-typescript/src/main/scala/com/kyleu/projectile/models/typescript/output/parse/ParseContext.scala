package com.kyleu.projectile.models.typescript.output.parse

import better.files.File

case class ParseContext(
    key: String,
    pkg: List[String],
    root: File
) {
  def plusPackage(s: String) = this.copy(pkg = pkg :+ s)
}
