package com.kyleu.projectile.models.database

object Statement {
  def adhoc(statement: String, vals: Any*) = new Statement {
    override def name = "adhoc"
    override def sql = statement
    override def values = vals
  }
}

trait Statement {
  def name: String = this.getClass.getSimpleName
  def sql: String
  def values: Seq[Any] = Seq.empty
}
