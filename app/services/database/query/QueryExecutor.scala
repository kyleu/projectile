package services.database.query

import java.sql.Connection

import models.database.query.Query.RawQuery
import models.database.query.Queryable

class QueryExecutor(val conn: Connection) extends Queryable {
  override def apply[A](query: RawQuery[A]) = apply[A](conn, query)
}
