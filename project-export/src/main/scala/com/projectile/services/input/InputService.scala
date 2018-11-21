package com.projectile.services.input

import com.projectile.models.command.ProjectileResponse
import com.projectile.models.database.input.{PostgresConnection, PostgresInput}
import com.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.projectile.services.config.ConfigService
import com.projectile.services.database.schema.SchemaHelper
import com.projectile.util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory

  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / s"input.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      loadFile[InputSummary](f, "input summary").copy(key = key)
    } else {
      throw new IllegalStateException(s"Cannot load input with key [$key]")
    }
  }

  def load(key: String): Input = {
    val summ = getSummary(key)
    summ.template match {
      case InputTemplate.Postgres => PostgresInputService.loadPostgres(cfg, summ)
      case t => throw new IllegalStateException(s"Unhandled template [$t]")
    }
  }

  def add(is: InputSummary) = {
    // TODO save summary
    val input = is.template match {
      case InputTemplate.Postgres =>
        PostgresInputService.toPostgresInput(summ = is, pc = PostgresConnection())
      case InputTemplate.Filesystem =>
        throw new IllegalStateException("Unable to save filesystem inputs (coming soon)")
    }
    PostgresInputService.savePostgres(cfg, input)
    input.asInstanceOf[Input]
  }

  def setPostgresOptions(key: String, conn: PostgresConnection) = {
    val input = PostgresInputService.toPostgresInput(summ = getSummary(key), pc = conn)
    PostgresInputService.savePostgres(cfg, input)
  }

  def remove(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  def refresh(key: String) = load(key) match {
    case pg: PostgresInput =>
      val conn = pg.newConnection()
      val s = SchemaHelper.calculateSchema(conn)
      conn.close()
      val pgi = pg.copy(enums = s.enums, tables = s.tables, views = s.views)
      PostgresInputService.savePostgres(cfg, pgi)
      pgi

    case x => throw new IllegalStateException(s"Unable to process [$x]")
  }
}
