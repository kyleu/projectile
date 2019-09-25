package com.kyleu.projectile.services.status

import java.time.LocalDate

import better.files.Resource

import scala.io.Source

object AppVersions {
  case class Version(on: LocalDate, v: (Int, Int, Int), title: String, desc: Option[String], features: Seq[(String, String, () => Option[String])])

  private[this] var versions = List.empty[Version]
  def getVersions = versions

  def clear() = versions = Nil

  def parseVersion(v: String) = v.split("\\.").toList match {
    case maj :: min :: patch :: Nil => (maj.toInt, min.toInt, patch.toInt)
    case _ => throw new IllegalStateException(s"Cannot parse version [$v]")
  }

  def addVersion(version: Version) = versions = (versions.filterNot(_.v == version.v) :+ version).sortBy(_.v)

  def register(on: String, v: String, title: String, desc: Option[String] = None, features: Seq[(String, String, () => Option[String])] = Nil) = {
    val ver = parseVersion(v)
    addVersion(Version(LocalDate.parse(on), ver, title, desc, features))
  }

  def registerMarkdown(path: String) = {
    val content = Resource.getAsString(path)
    var pending = List.empty[String]
    var sections = List.empty[Seq[String]]
    Source.fromString(content).getLines().filter(_.nonEmpty).map(_.trim).foreach { l =>
      if (l.startsWith("#")) {
        if (pending.nonEmpty) {
          sections = sections :+ pending
        }
      }
      pending = pending :+ l
    }
    if (pending.nonEmpty) {
      sections = sections :+ pending
    }
    val finished = sections.map {
      case h :: t =>
        val (v, on, title) = h.split("/").map(_.trim).filter(_.nonEmpty).toList match {
          case ver :: onStr :: title :: Nil => (ver.stripPrefix("#").trim(), onStr, title)
          case _ => throw new IllegalStateException(s"Cannot parse [$h] from [$path}]")
        }
        val desc = t.headOption.filterNot(_.startsWith("-"))
        val features = t.filter(_.startsWith("-")).map { x =>
          ("", x, () => None)
        }
        Version(LocalDate.parse(on), parseVersion(v), title, desc, features)
      case _ => throw new IllegalStateException("Docs?")
    }
    finished.foreach(addVersion)
  }
}
