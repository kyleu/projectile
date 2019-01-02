package com.kyleu.projectile.models.database

trait Row {
  def asOpt[T](s: String): Option[T]
  def as[T](s: String): T

  def toSeq: Seq[Option[AnyRef]]
}
