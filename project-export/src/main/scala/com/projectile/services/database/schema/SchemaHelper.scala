package com.projectile.services.database.schema

import java.sql.Connection

import com.projectile.models.database.schema.Schema
import com.projectile.util.Logging

object SchemaHelper extends Logging {
  def calculateSchema(conn: Connection) = {
    val catalogName = Option(conn.getCatalog)
    val schemaName = try {
      Option(conn.getSchema)
    } catch {
      case _: AbstractMethodError => None
    }
    val metadata = conn.getMetaData

    val schemaModel = Schema(
      schemaName = schemaName,
      catalog = catalogName,
      url = metadata.getURL,
      username = metadata.getUserName,
      engineVersion = metadata.getDatabaseProductVersion,
      timezone = 0,
      enums = Nil,
      tables = Nil,
      views = Nil
    )

    val timezone = MetadataTimezone.getTimezone(conn)
    val enums = MetadataEnums.getEnums(conn)
    val tables = MetadataTables.getTables(metadata, catalogName, schemaName)
    val views = MetadataViews.getViews(metadata, catalogName, schemaName)

    schemaModel.copy(
      timezone = timezone,
      enums = enums,
      tables = MetadataTables.withTableDetails(conn, metadata, tables, enums),
      views = MetadataViews.withViewDetails(metadata, views, enums),
      detailsLoadedAt = Some(System.currentTimeMillis)
    )
  }
}
