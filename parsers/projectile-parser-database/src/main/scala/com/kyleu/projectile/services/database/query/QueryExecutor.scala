package com.kyleu.projectile.services.database.query

import java.sql.Connection

import com.kyleu.projectile.models.database.query.Query.RawQuery
import com.kyleu.projectile.models.database.query.Queryable
import com.kyleu.projectile.util.tracing.TraceData

class QueryExecutor(val conn: Connection) extends Queryable {
  override def apply[A](query: RawQuery[A])(implicit td: TraceData) = apply[A](conn, query)
}
