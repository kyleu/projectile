package com.kyleu.projectile.services.typescript

import better.files.File

case class TypeScriptServiceParams(
    root: File,
    cache: File,
    path: String,
    sourcecode: String,
    depth: Int,
    parseRefs: Boolean,
    encountered: Set[String],
    messages: Seq[String]
) {
  val file = root / path
}
