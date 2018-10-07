package services.input

import better.files.File
import models.command.ProjectileResponse
import models.input.Input
import services.config.ConfigService

class InputService(val cfg: ConfigService) {
  val dir = cfg.inputDirectory

  def loadInput(inputDir: File) = {
    val key = inputDir.name.stripSuffix(".json")
    Input(key = key, title = "TODO", description = "TODO")
  }

  val inputs = dir.children.filter(_.isDirectory).map(loadInput).toSeq

  def list() = ProjectileResponse.InputList(inputs)

  def get(key: String) = ProjectileResponse.InputDetail(
    inputs.find(_.key == key).getOrElse(throw new IllegalStateException(s"Cannot find configuration for input [$key]"))
  )
}
