package com.kyleu.projectile.services.input

import com.kyleu.projectile.models.input.InputSummary
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.JsonSerializers._

object SummaryInputService {
  private[this] val fn = "input.json"

  def saveSummary(cfg: ConfigService, summary: InputSummary) = {
    val dir = cfg.inputDirectory / summary.key
    dir.createDirectories()

    val summ = printJson(summary.asJson)
    (dir / fn).overwrite(summ)

    dir
  }
}
