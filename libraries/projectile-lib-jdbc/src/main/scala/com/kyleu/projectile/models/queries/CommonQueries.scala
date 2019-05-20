package com.kyleu.projectile.models.queries

import com.kyleu.projectile.models.database.{Row, SingleRowQuery}

object CommonQueries {
  case class DoesTableExist(tableName: String) extends SingleRowQuery[Boolean] {
    override val sql = "select count(*) as c from information_schema.tables WHERE (table_name = ? or table_name = ?);"
    override val values = tableName :: tableName.toUpperCase :: Nil
    override def map(row: Row) = row.as[Long]("c") > 0
  }
}
