package com.kyleu.projectile.services.typescript

import better.files.File

case class ServiceParams(
    root: File,
    cache: File,
    path: String,
    sourcecode: String,
    depth: Int,
    parseRefs: Boolean,
    forceCompile: Boolean,
    messages: Seq[String]
) {
  def plus(i: Int = 1) = copy(depth = depth + i)
  val file = root / path
}
