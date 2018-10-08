package services.input

import io.scalaland.chimney.dsl._
import models.database.input.{PostgresConnection, PostgresInput}
import models.database.schema.{EnumType, Table, View}
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
      case InputTemplate.Postgres => loadPostgres(summ)
      case t => throw new IllegalStateException(s"Unhandled template [$t]")
    }
  }

  def add(i: InputSummary) = {
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
      conn.close()
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

    if (pgi.enums.nonEmpty) {
      val enumDir = dir / pgi.key / "enums"
      enumDir.createDirectories()
      pgi.enums.foreach(e => (enumDir / s"${e.key}.json").overwrite(e.asJson.spaces2))
    }

    if (pgi.tables.nonEmpty) {
      val tableDir = dir / pgi.key / "tables"
      tableDir.createDirectories()
      pgi.tables.foreach(t => (tableDir / s"${t.name}.json").overwrite(t.asJson.spaces2))
    }

    if (pgi.views.nonEmpty) {
      val viewDir = dir / pgi.key / "views"
      viewDir.createDirectories()
      pgi.views.foreach(v => (viewDir / s"${v.name}.json").overwrite(v.asJson.spaces2))
    }
  }

  private[this] def loadPostgres(summ: InputSummary) = {
    val pis = loadFile[PostgresConnection](dir / summ.key / "dbconn.json", "Postgres connection")

    def loadDir[A: Decoder](k: String) = {
      val d = dir / summ.key / k
      if (d.exists && d.isDirectory && d.isReadable) {
        d.children.map(f => loadFile[A](f, k)).toList
      } else {
        Nil
      }
    }

    summ.into[PostgresInput]
      .withFieldComputed(_.url, _ => pis.url)
      .withFieldComputed(_.username, _ => pis.username)
      .withFieldComputed(_.password, _ => pis.password)
      .withFieldComputed(_.db, _ => pis.db)
      .withFieldComputed(_.catalog, _ => pis.catalog)
      .withFieldComputed(_.enums, _ => loadDir[EnumType]("enums"))
      .withFieldComputed(_.tables, _ => loadDir[Table]("tables"))
      .withFieldComputed(_.views, _ => loadDir[View]("views"))
      .transform
  }
}
