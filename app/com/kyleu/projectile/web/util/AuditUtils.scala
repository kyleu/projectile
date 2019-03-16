package com.kyleu.projectile.web.util

import better.files.File

object AuditUtils {
  private[this] val root = File("./tmp/audit")

  case class FileResult(fn: String, l: Int, o: String, n: String)
  case class AuditResult(k: String, f: String, r: Seq[FileResult])

  def saveAudit(k: String, f: String): Unit = {
    val auditResults = root / k / f
    val comparison = srcDir(k, f)
    auditResults.delete(swallowIOExceptions = true)
    comparison.copyTo(auditResults)
  }

  def auditAll() = {
    kids(root).flatMap(auditCatgegory)
  }

  def auditCatgegory(k: String) = {
    kids(root / k).map(audit(k, _))
  }

  def audit(k: String, f: String) = AuditResult(k = k, f = f, r = getResult(s = root / k / f, t = srcDir(k, f)))

  private[this] val badNames = Set(".DS_Store")
  private[this] def kids(d: File) = d.children.filter(_.isDirectory).map(c => c.name).filterNot(x => x == "target" || x == "project").toList.sorted
  private[this] def srcDir(k: String, f: String) = File(FilesystemUtils.tgtDir) / k / f / "src" / "main" / "scala"

  private[this] def getResult(s: File, t: File, stack: Seq[String] = Nil): Seq[FileResult] = if (s.isDirectory) {
    s.children.filterNot(c => badNames(c.name)).flatMap(c => getResult(c, t / c.name, stack :+ t.name)).toList
  } else if (!t.exists) {
    val name = (stack.drop(1) :+ t.name).mkString("/")
    Seq(FileResult(fn = name, l = 0, o = s"Target file is missing", n = "..."))
  } else {
    val sc = s.contentAsString
    val tc = t.contentAsString
    val name = (stack.drop(1) :+ t.name).mkString("/")
    if (sc == tc) {
      Nil
    } else {
      val sl = scala.io.Source.fromString(sc).getLines.toList
      val tl = scala.io.Source.fromString(tc).getLines.toList
      val diffs = sl.zip(tl).zipWithIndex.flatMap(x => if (x._1._1 == x._1._2) { None } else { Some(x) })
      if (diffs.isEmpty) {
        Nil
      } else if (diffs.size > 10) {
        Seq(FileResult(fn = name, l = 0, o = s"[${diffs.size}] differences", n = "..."))
      } else {
        diffs.map { d =>
          FileResult(fn = name, l = d._2, o = d._1._1, n = d._1._2)
        }
      }
    }
  }
}
