package services.input

import models.input.InputSummary
import services.config.ConfigService
import util.JsonSerializers._

class InputService(val cfg: ConfigService) {
  private[this] val dir = cfg.inputDirectory
  private[this] val fn = "input.json"

  def list() = dir.children.toSeq.map(_.name.stripSuffix(".json")).sorted.map(getSummary)

  def getSummary(key: String) = {
    val f = dir / key / s"input.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[InputSummary](f.contentAsString) match {
        case Right(is) => is
        case Left(x) => throw x
      }
    } else {
      throw new IllegalStateException(s"Cannot load [$fn] for project [$key]")
    }
  }

  def refresh(key: String) = {
    getSummary(key)
  }
}
