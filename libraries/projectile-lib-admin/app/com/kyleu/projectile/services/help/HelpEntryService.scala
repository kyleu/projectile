package com.kyleu.projectile.services.help

import better.files.Resource
import com.kyleu.projectile.util.Logging

object HelpEntryService extends Logging {
  def contentFor(path: String*) = {
    val fn = ("help" +: (if (path.isEmpty) { Seq("index") } else { path })).mkString("/") + ".html"
    Resource.asString(fn)
  }
}
