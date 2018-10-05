package services.database.schema

import java.sql.Connection

import models.database.schema.Schema
import util.Logging

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
      driver = metadata.getDriverName,
      driverVersion = metadata.getDriverVersion,
      catalogTerm = metadata.getCatalogTerm,
      schemaTerm = metadata.getSchemaTerm,
      procedureTerm = metadata.getProcedureTerm,
      maxSqlLength = metadata.getMaxStatementLength,
      timezone = 0,
      enums = Nil,
      tables = Nil,
      views = Nil,
      procedures = Nil
    )

    val timezone = MetadataTimezone.getTimezone(conn)
    val enums = MetadataEnums.getEnums(conn)
    val tables = MetadataTables.getTables(metadata, catalogName, schemaName)
    val views = MetadataViews.getViews(metadata, catalogName, schemaName)
    val procedures = MetadataProcedures.getProcedures(metadata, catalogName, schemaName)

    val schema = schemaModel.copy(
      timezone = timezone,
      enums = enums,
      tables = tables,
      views = views,
      procedures = procedures
    )

    schema
  }
}
