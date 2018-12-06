package com.projectile.services.input

import better.files.File
import io.scalaland.chimney.dsl._
import com.projectile.models.database.input.{PostgresConnection, PostgresInput}
import com.projectile.models.database.schema.{EnumType, Table, View}
import com.projectile.models.input.{InputSummary, InputTemplate}
import com.projectile.services.config.ConfigService
import com.projectile.util.JsonSerializers._

import scala.util.control.NonFatal

object PostgresInputService {
  private[this] val fn = "dbconn.json"

  def savePostgresDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(PostgresConnection().asJson.spaces2)
  }

  def savePostgres(cfg: ConfigService, pgi: PostgresInput) = {
    val summ = pgi.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.Postgres).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val dbconn = pgi.into[PostgresConnection].transform.asJson.spaces2
    (dir / fn).overwrite(dbconn)

    if (pgi.enums.nonEmpty) {
      val enumDir = dir / "enum"
      enumDir.createDirectories()
      pgi.enums.foreach(e => (enumDir / s"${e.key}.json").overwrite(e.asJson.spaces2))
    }

    if (pgi.tables.nonEmpty) {
      val tableDir = dir / "table"
      tableDir.createDirectories()
      pgi.tables.foreach(t => (tableDir / s"${t.name}.json").overwrite(t.asJson.spaces2))
    }

    if (pgi.views.nonEmpty) {
      val viewDir = dir / "view"
      viewDir.createDirectories()
      pgi.views.foreach(v => (viewDir / s"${v.name}.json").overwrite(v.asJson.spaces2))
    }

    pgi
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

    val pc = loadFile[PostgresConnection](dir / fn, "Postgres connection")

    def loadDir[A: Decoder](k: String) = {
      val d = dir / k
      if (d.exists && d.isDirectory && d.isReadable) {
        d.children.map(f => try {
          loadFile[A](f, k)
        } catch {
          case NonFatal(x) => throw new IllegalStateException(s"Error loading postgres file [${f.pathAsString}]", x)
        }).toList
      } else {
        Nil
      }
    }

    toPostgresInput(summ, pc, loadDir[EnumType]("enum"), loadDir[Table]("table"), loadDir[View]("view"))
  }
}
