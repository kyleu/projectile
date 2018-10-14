package models.output.feature.wiki

import models.export.config.ExportConfiguration
import models.output.feature.Feature
import models.output.file.OutputFile

object WikiLogic extends Feature.Logic {

  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    Seq.empty[OutputFile.Rendered]
  }
}
