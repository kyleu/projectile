package models.output.feature.wiki

import models.export.config.ExportConfiguration
import models.output.feature.Feature
import models.output.file.OutputFile

object WikiLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, verbose: Boolean) = {
    (Seq.empty[OutputFile.Rendered], Seq.empty[String])
  }
}
