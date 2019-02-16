package com.kyleu.projectile.models.database

trait Statement {
  def name: String = this.getClass.getSimpleName
  def sql: String
  def values: Seq[Any] = Seq.empty
}
