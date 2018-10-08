package services.input

import java.sql.Connection

import io.scalaland.chimney.dsl._
import models.database.input.{PostgresInput, PostgresConnection}
import models.input.{Input, InputSummary, InputTemplate}
import services.config.ConfigService
import services.database.schema.SchemaHelper
import util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory
  private[this] val fn = "input.json"

  def list() = dir.children.toList.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = loadFile[InputSummary](dir / key / s"input.json", "input summary")

  def load(key: String): Input = {
    val summ = getSummary(key)
    summ.t match {
      case InputTemplate.Postgres => toPostgres(summ)
      case t => throw new IllegalStateException(s"Unhandled template [$t]")
    }
  }

  def save(i: InputSummary) = {
    // TODO
    i
  }

  def remove(key: String) = {
    // TODO
    getSummary(key)
  }

  def refresh(key: String) = load(key) match {
    case pg: PostgresInput =>
      val conn = pg.newConnection()
      val s = SchemaHelper.calculateSchema(conn)
      val pgi = pg.copy(enums = s.enums, tables = s.tables, views = s.views)
      savePostgres(pgi)
      pgi

    case x => throw new IllegalStateException(s"Unable to process [$x]")
  }

  private[this] def savePostgres(pgi: PostgresInput) = {
    val summ = pgi.into[InputSummary].withFieldComputed(_.t, _ => InputTemplate.Postgres).transform.asJson.spaces2
    (dir / pgi.key / fn).overwrite(summ)

    val dbconn = pgi.into[PostgresConnection].transform.asJson.spaces2
    (dir / pgi.key / "dbconn.json").overwrite(dbconn)

    val tableDir = dir / pgi.key / "tables"
    tableDir.createDirectories()
    pgi.tables.foreach { t =>
      (tableDir / s"${t.name}.json").overwrite(t.asJson.spaces2)
    }

    val viewDir = dir / pgi.key / "views"
    viewDir.createDirectories()
    pgi.views.foreach { v =>
      (viewDir / s"${v.name}.json").overwrite(v.asJson.spaces2)
    }
  }

  private[this] def toPostgres(summ: InputSummary) = {
    val pis = loadFile[PostgresConnection](dir / summ.key / "dbconn.json", "Postgres connection")
    summ.into[PostgresInput]
      .withFieldComputed(_.url, _ => pis.url)
      .withFieldComputed(_.username, _ => pis.username)
      .withFieldComputed(_.password, _ => pis.password)
      .withFieldComputed(_.db, _ => pis.db)
      .withFieldComputed(_.catalog, _ => pis.catalog)
      .withFieldComputed(_.enums, _ => Nil)
      .withFieldComputed(_.tables, _ => Nil)
      .withFieldComputed(_.views, _ => Nil)
      .transform
  }
}
