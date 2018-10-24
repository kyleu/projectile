package models.output.feature.graphql

import models.export.config.ExportConfiguration
import models.output.feature.Feature
import models.output.file.OutputFile

object GraphQLLogic extends Feature.Logic {
  override def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit) = {
    Seq.empty[OutputFile.Rendered]
  }
}
