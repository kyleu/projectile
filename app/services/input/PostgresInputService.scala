package services.input

import io.scalaland.chimney.dsl._

import models.database.input.{PostgresConnection, PostgresInput}
import models.database.schema.{EnumType, Table, View}
import models.input.{InputSummary, InputTemplate}
import services.config.ConfigService
import util.JsonSerializers._

object PostgresInputService {
  private[this] val fn = "input.json"

  def savePostgres(cfg: ConfigService, pgi: PostgresInput) = {
    val dir = cfg.inputDirectory / pgi.key
    dir.createDirectories()

    val summ = pgi.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.Postgres).transform.asJson.spaces2
    (dir / fn).overwrite(summ)

    val dbconn = pgi.into[PostgresConnection].transform.asJson.spaces2
    (dir / "dbconn.json").overwrite(dbconn)

    if (pgi.enums.nonEmpty) {
      val enumDir = dir / "enums"
      enumDir.createDirectories()
      pgi.enums.foreach(e => (enumDir / s"${e.key}.json").overwrite(e.asJson.spaces2))
    }

    if (pgi.tables.nonEmpty) {
      val tableDir = dir / "tables"
      tableDir.createDirectories()
      pgi.tables.foreach(t => (tableDir / s"${t.name}.json").overwrite(t.asJson.spaces2))
    }

    if (pgi.views.nonEmpty) {
      val viewDir = dir / "views"
      viewDir.createDirectories()
      pgi.views.foreach(v => (viewDir / s"${v.name}.json").overwrite(v.asJson.spaces2))
    }
  }

  def toPostgresInput(summ: InputSummary, pc: PostgresConnection, enums: Seq[EnumType] = Nil, tables: Seq[Table] = Nil, views: Seq[View] = Nil) = {
    summ.into[PostgresInput]
      .withFieldComputed(_.url, _ => pc.url).withFieldComputed(_.username, _ => pc.username).withFieldComputed(_.password, _ => pc.password)
      .withFieldComputed(_.db, _ => pc.db).withFieldComputed(_.catalog, _ => pc.catalog)
      .withFieldComputed(_.enums, _ => enums).withFieldComputed(_.tables, _ => tables).withFieldComputed(_.views, _ => views)
      .transform
  }

  def loadPostgres(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = loadFile[PostgresConnection](dir / "dbconn.json", "Postgres connection")

    def loadDir[A: Decoder](k: String) = {
      val d = dir / k
      if (d.exists && d.isDirectory && d.isReadable) {
        d.children.map(f => loadFile[A](f, k)).toList
      } else {
        Nil
      }
    }

    toPostgresInput(summ, pc, loadDir[EnumType]("enums"), loadDir[Table]("tables"), loadDir[View]("views"))
  }
}
