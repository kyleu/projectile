package com.kyleu.projectile.models.database.query

import java.sql.ResultSet

import com.kyleu.projectile.services.database.query.JdbcRow

object Query {
  trait RawQuery[A] {
    def sql: String
    def values: Seq[Any] = Seq.empty
    def handle(results: ResultSet): A
  }

  trait SingleRowQuery[A] extends Query[A] {
    def map(row: JdbcRow): A
    override final def reduce(rows: Iterator[JdbcRow]) = if (rows.hasNext) {
      rows.map(map).next()
    } else {
      throw new IllegalStateException(s"No row returned for [$sql].")
    }
  }

  trait FlatSingleRowQuery[A] extends Query[Option[A]] {
    def flatMap(row: JdbcRow): Option[A]
    override final def reduce(rows: Iterator[JdbcRow]) = if (rows.hasNext) { flatMap(rows.next()) } else { None }
  }
}

trait Query[A] extends Query.RawQuery[A] {
  override def handle(results: ResultSet) = reduce(new JdbcRow.Iter(results))
  def reduce(rows: Iterator[JdbcRow]): A
}
