package services.input

import io.scalaland.chimney.dsl._
import models.input.{InputSummary, PostgresInput, PostgresInputSummary}
import services.config.ConfigService
import util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory
  private[this] val fn = "input.json"

  def list() = dir.children.toSeq.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = loadFile[InputSummary](dir / key / s"input.json", "")

  def load(key: String) = {
    val summ = getSummary(key)
    summ.t match {
      case PostgresInput.key => toPostgres(summ)
      case t => throw new IllegalStateException(s"Unhandled template [$t]")
    }
  }

  def refresh(key: String) = {
    getSummary(key)
  }

  private[this] def toPostgres(summ: InputSummary) = {
    val pis = loadFile[PostgresInputSummary](dir / summ.key / s"dbconn.json", "")
    summ.into[PostgresInput]
      .withFieldComputed(_.url, _ => pis.url)
      .withFieldComputed(_.username, _ => pis.username)
      .withFieldComputed(_.password, _ => pis.password)
      .withFieldComputed(_.db, _ => pis.db)
      .withFieldComputed(_.catalog, _ => pis.catalog)
      .withFieldComputed(_.enums, _ => Nil)
      .withFieldComputed(_.tables, _ => Nil)
      .withFieldComputed(_.views, _ => Nil)
      .withFieldComputed(_.procedures, _ => Nil)
      .transform
  }
}
