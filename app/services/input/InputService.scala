package services.input

import models.input.Input
import services.config.{ConfigService, FileCacheService}

class InputService(val cfg: ConfigService) extends FileCacheService[Input](cfg.inputDirectory, "Input") {
  override def newModel(key: String, title: String, description: String) = Input(key = key, title = title, description = description)

  refresh()
}
