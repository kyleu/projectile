package com.projectile.services.input

import com.projectile.models.input.InputSummary
import com.projectile.services.config.ConfigService
import com.projectile.util.JsonSerializers._

object SummaryInputService {
  private[this] val fn = "input.json"

  def saveSummary(cfg: ConfigService, summary: InputSummary) = {
    val dir = cfg.inputDirectory / summary.key
    dir.createDirectories()

    val summ = summary.asJson.spaces2
    (dir / fn).overwrite(summ)

    dir
  }
}
