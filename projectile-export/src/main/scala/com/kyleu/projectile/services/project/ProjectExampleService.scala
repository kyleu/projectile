package com.kyleu.projectile.services.project

import better.files.{File, Resource}
import com.kyleu.projectile.services.project.audit.ExportValidation
import com.kyleu.projectile.util.Version
import org.apache.commons.io.IOUtils

object ProjectExampleService {
  case class ExampleProject(key: String, name: String, description: String)

  val dir = "com/kyleu/projectile/example"

  val projects = Seq(
    ExampleProject("admin", "Admin web application", ""),
    ExampleProject("graphql", "GraphQL application", ""),
    ExampleProject("scalajs", "Scala.js application", ""),
    ExampleProject("thrift", "Thrift application", "")
  )

  def extract(project: String, to: File, name: String, force: Boolean = false) = {
    if (name.contains('.') || name.contains('-') || name.contains('_')) {
      throw new IllegalStateException(s"Project key [$name] may not contain punctuation")
    }

    val is = Resource.getAsStream(s"$dir/$project.zip")

    val f = File.newTemporaryFile()
    f.writeByteArray(IOUtils.toByteArray(is))
    f.unzipTo(to)
    f.delete()

    val replacements = Map("_PROJECT_NAME" -> name, "_PROJECTILE_VERSION" -> Version.version)
    def replaceToken(f: File): Unit = if (!ExportValidation.badBoys(f.name)) {
      if (f.isDirectory) {
        f.children.foreach(replaceToken)
      } else if (ExportValidation.extensions.exists(f.name.endsWith)) {
        val orig = f.contentAsString
        val n = replacements.foldLeft(orig)((l, r) => l.replaceAllLiterally(r._1, r._2))
        if (orig != n) { f.overwrite(n) }
      }
    }
    replaceToken(to)
    true
  }
}
