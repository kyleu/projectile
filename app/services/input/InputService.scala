package services.input

import models.database.input.{PostgresConnection, PostgresInput}
import models.input.{Input, InputSummary, InputTemplate}
import services.config.ConfigService
import services.database.PostgresInputService
import services.database.schema.SchemaHelper
import util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory
  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = loadFile[InputSummary](dir / key / s"input.json", "input summary")

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
      case InputTemplate.Postgres => PostgresInputService.toPostgresInput(summ = is, pc = PostgresConnection())
      case InputTemplate.Filesystem => throw new IllegalStateException("Unable to save filesystem inputs (coming soon)")
    }
    PostgresInputService.savePostgres(cfg, input)
    input.asInstanceOf[Input]
  }

  def remove(key: String) = {
    // TODO
    getSummary(key)
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
