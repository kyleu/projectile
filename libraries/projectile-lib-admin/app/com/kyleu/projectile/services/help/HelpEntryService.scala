package com.kyleu.projectile.services.help

import better.files.Resource
import com.kyleu.projectile.util.Logging

object HelpEntryService extends Logging {
  def contentFor(path: String*) = {
    val trimmed = path.map(_.trim).filter(_.nonEmpty)
    val fn = ("help" +: (if (trimmed.isEmpty) { Seq("index") } else { trimmed })).mkString("/") + ".html"
    Resource.asString(fn)
  }

  def hasHelp(path: String*): Option[Seq[String]] = contentFor(path: _*).map(_ => path).orElse {
    if (path.size > 1) { hasHelp(path.dropRight(1): _*) } else { None }
  }
}
