package com.kyleu.projectile.models.template

object Icons {
  private[this] def fa(key: String) = s"fa fa-$key"

  val selected = fa("check-circle-o")
  val empty = fa("circle-o")

  val result = fa("check")
  val projectile = fa("gears")

  val project = fa("gear")
  val library = fa("calendar-o")
  val web = fa("calendar")

  val input = fa("star-o")

  val file = fa("file-o")
  val inject = fa("magnet")

  val thrift = fa("tumblr")
  val database = fa("database")
  val typeScript = fa("flag-o")

  val enum = fa("tag")

  val model = fa("puzzle-piece")
  val union = fa("magnet")
  val service = fa("rocket")

  val table = fa("folder-open-o")
  val view = fa("bar-chart")

  val logs = fa("reorder")

  val graphql = fa("bolt")
  val json = fa("wrench")
  val scala = fa("sitemap")
  val markdown = fa("bookmark-o")

  val audit = fa("trophy")

}
