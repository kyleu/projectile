package com.kyleu.projectile.services.project

import better.files.{File, Resource}
import com.kyleu.projectile.util.NullUtils

object ProjectExampleService {
  case class ExampleProject(key: String, name: String, description: String)

  val dir = "com/kyleu/projectile/example"

  val projects = Seq(
    // Database
    ExampleProject("play", "Play Framework web application", ""),
    ExampleProject("admin", "Admin web application", ""),
    ExampleProject("websocket", "Web application with Scala.js", ""),

    // GraphQL
    ExampleProject("graphql", "GraphQL application", ""),

    // Thrift
    ExampleProject("thrift", "Thrift application", ""),

    // TypeScript
    ExampleProject("scalajs", "Scala.js application", "")
  )

  def extract(project: String, to: File, name: String) = {
    val is = Resource.getAsStream(s"$dir/$project.zip")
    val zis = new java.util.zip.ZipInputStream(is)

    var ret = Seq.empty[(String, Int)]
    Stream.continually(zis.getNextEntry).takeWhile(_ != NullUtils.inst).foreach { file =>
      val fOut = to / file.getName
      if (!file.isDirectory) {
        fOut.createIfNotExists(asDirectory = file.isDirectory, createParents = true)
        val buffer = new Array[Byte](1024)
        Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(_ => fOut.writeByteArray(buffer.reverse.dropWhile(_ == 0).reverse))
        ret = ret :+ (file.getName -> fOut.size.toInt)
      }
    }
    val replacements = Map("project" -> name)
    def replaceToken(f: File): Unit = if (f.isDirectory) {
      f.name match {
        case "target" => // noop
        case _ => f.children.foreach(replaceToken)
      }
    } else {
      val orig = f.contentAsString
      val n = replacements.foldLeft(orig)((l, r) => l.replaceAllLiterally("{{" + r._1 + "}}", r._2))
      if (orig != n) { f.overwrite(n) }
    }
    replaceToken(to)
    ret.sortBy(_._1)
  }
}
