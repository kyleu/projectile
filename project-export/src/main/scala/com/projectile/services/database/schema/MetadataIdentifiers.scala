package com.projectile.services.database.schema

import java.sql.DatabaseMetaData

import com.projectile.services.database.query.JdbcRow

object MetadataIdentifiers {
  def getRowIdentifier(metadata: DatabaseMetaData, catalog: Option[String], schema: Option[String], name: String) = {
    val rs = metadata.getBestRowIdentifier(catalog.orNull, schema.orNull, name, DatabaseMetaData.bestRowSession, true)
    new JdbcRow.Iter(rs).map(_.as[String]("COLUMN_NAME")).toList
  }
}
