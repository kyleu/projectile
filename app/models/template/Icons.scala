package models.template

object Icons {
  private[this] def fa(key: String) = s"fa fa-$key"

  val result = fa("check")
  val projectile = fa("gears")

  val project = fa("gear")
  val library = fa("calendar-o")
  val web = fa("calendar")

  val input = fa("star-o")

  val file = fa("file-o")

  val database = fa("database")

  val enum = fa("tag")

  val model = fa("puzzle-piece")
  val service = fa("rocket")

  val table = fa("folder-open-o")
  val view = fa("bar-chart")
}
