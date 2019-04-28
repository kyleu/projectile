package com.kyleu.projectile.web.controllers

import com.kyleu.projectile.models.config._

import scala.concurrent.Future

@javax.inject.Singleton
class TestController @javax.inject.Inject() () extends ProjectileController {
  def list() = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.test.testlist(projectile, Seq("deftyped"))))
  }

  def test(key: String, theme: String) = Action.async { implicit request =>
    val rsp = key match {
      case "deftyped" => sortDefinitelyTyped()
      case _ => throw new IllegalStateException(s"Unhandled test [$key]")
    }
    Future.successful(Ok(rsp))
  }

  private[this] def sortDefinitelyTyped() = {
    val chunkSize = 100
    val r = better.files.File("./out/typescript-projects")
    val f = r / "list.txt"
    val dir = r / "deftyped"
    val children = dir.children.filter(_.isDirectory).filterNot(_.name == "project")

    val data = children.toList.sortBy(_.name).map(c => c.name -> loadDeps(c))
    val roots = data.filter(_._2.isEmpty).map(_._1)

    val (nodeps, deps) = data.partition(_._2.isEmpty)

    var acc = nodeps.map(_._1).toSet
    var remaining = deps
    var tiers = nodeps :: Nil

    while (remaining.nonEmpty) {
      val (hit, miss) = remaining.partition(_._2.forall(acc.apply))
      if (hit.isEmpty) {
        // throw new IllegalStateException(s"Unable to match [${miss.size}] projects. First offender: ${miss.headOption}")
        remaining = Nil
        tiers = tiers :+ miss.map(m => m._1 -> m._2.filterNot(acc.apply))
      } else {
        acc = acc ++ hit.map(_._1)
        remaining = miss
        tiers = tiers :+ hit
      }
    }

    // val content = data.map(x => x._1 + ": " + x._2.sorted.mkString(", ")).mkString("\n")
    val content = tiers.zipWithIndex.flatMap { t =>
      val content = if (t._1.size > chunkSize) {
        t._1.grouped(chunkSize).zipWithIndex.flatMap { chunk =>
          s"  Chunk [${chunk._2}]:" +: chunk._1.map(x => "    " + x._1 + ": " + x._2.sorted.mkString(", ")) :+ ""
        }.toList
      } else {
        t._1.map(x => "  " + x._1 + ": " + x._2.sorted.mkString(", "))
      }
      s"Tier ${t._2} [${t._1.size} members]:" +: content :+ ""
    }.mkString("\n")

    f.overwrite(content)
    play.twirl.api.Html("<pre>" + content + "</pre>")
  }

  private[this] def loadDeps(child: better.files.File) = {
    val buildFile = child / "build.sbt"
    val buildFileContent = buildFile.lines.filter(l => l.contains("\"com.definitelyscala\"") && l.contains("%%"))
    buildFileContent.map(l => l.substring(l.indexOf("%%") + 2, l.lastIndexOf("%")).replaceAllLiterally("\"", "").trim).toList
  }
}
