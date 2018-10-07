package models.template

object Icons {
  private[this] def fa(key: String) = s"fa fa-$key"

  val result = fa("check")
  val projectile = fa("gears")
  val project = fa("gear")
  val input = fa("star-o")
}
