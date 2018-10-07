package services.input

import models.input.InputSummary
import services.config.{ConfigService, FileCacheService}

class InputService(val cfg: ConfigService) extends FileCacheService[InputSummary](cfg.inputDirectory, "Input") {
  override def newModel(key: String, title: String, description: String) = InputSummary(t = "filesystem", key = key, title = title, description = description)

  refresh()
}
