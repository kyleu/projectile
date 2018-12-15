package com.kyleu.projectile.services.database.query

import java.sql.Connection

import com.kyleu.projectile.models.database.query.Query.RawQuery
import com.kyleu.projectile.models.database.query.Queryable

class QueryExecutor(val conn: Connection) extends Queryable {
  override def apply[A](query: RawQuery[A]) = apply[A](conn, query)
}
