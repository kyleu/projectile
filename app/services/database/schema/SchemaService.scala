package services.database.schema

import java.sql.Connection
import java.util.UUID

import models.database.schema.Schema
import util.Logging

object SchemaService extends Logging {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty

  def get(connectionId: UUID) = schemaMap.get(connectionId)

  def set(connectionId: UUID, schema: Schema) = {
    schemaMap = schemaMap + (connectionId -> schema)
  }

  def getSchema(conn: Connection) = {
    SchemaHelper.calculateSchema(conn)
  }

  def getTable(connectionId: UUID, name: String) = get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = get(connectionId).flatMap(_.views.find(_.name == name))
  def getProcedure(connectionId: UUID, name: String) = get(connectionId).flatMap(_.procedures.find(_.name == name))
}
