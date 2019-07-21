package com.kyleu.projectile.services.database.schema

import java.sql.Connection
import java.util.UUID

import com.kyleu.projectile.models.database.schema.Schema
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData

object SchemaService extends Logging {
  private[this] var schemaMap: Map[UUID, Schema] = Map.empty

  def get(connectionId: UUID) = schemaMap.get(connectionId)

  def set(connectionId: UUID, schema: Schema) = {
    schemaMap = schemaMap + (connectionId -> schema)
  }

  def getSchema(conn: Connection)(implicit td: TraceData) = {
    SchemaHelper.calculateSchema(conn)
  }

  def getTable(connectionId: UUID, name: String) = get(connectionId).flatMap(_.tables.find(_.name == name))
  def getView(connectionId: UUID, name: String) = get(connectionId).flatMap(_.views.find(_.name == name))
}
