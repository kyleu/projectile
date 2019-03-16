package com.kyleu.projectile.web.util

import com.kyleu.projectile.services.project.ProjectExampleService

object ExampleProjectHelper {
  private[this] def dir(k: String) = {
    val f = better.files.File(k)
    if (!f.exists) { throw new IllegalStateException(s"Cannot load directory [${f.pathAsString}]") }
    f
  }

  private[this] val in = dir("examples")
  private[this] val out = dir(s"projectile-export/src/main/resources/${ProjectExampleService.dir}")

  def listFiles() = in.children.filter(_.isDirectory).map(_.name).filterNot(_.startsWith("test")).toList.sorted

  def compileAll() = listFiles().map(compile)

  def compile(k: String) = {
    val f = in / k
    val zipped = f.zipTo(out / (k + ".zip"))
    k -> zipped.size.toInt
  }
}
