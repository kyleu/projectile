package models.output.feature.doobie

import models.export.config.ExportConfiguration
import models.output.feature.Feature
import models.output.file.OutputFile

object DoobieLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    Seq.empty[OutputFile.Rendered]
  }
}
