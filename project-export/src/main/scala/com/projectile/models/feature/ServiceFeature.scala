package com.projectile.models.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.OutputFile

sealed abstract class ServiceFeature(
    override val value: String,
    val title: String,
    val description: String,
    val dependsOn: Set[ProjectFeature] = Set(ProjectFeature.Core)
) extends StringEnumEntry

object ServiceFeature extends StringEnum[ServiceFeature] with StringCirceEnum[ServiceFeature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends ServiceFeature(
    value = "core", title = "Core", description = "Scala service class wrapping Thrift's MessagePerEndpoint"
  )

  override val values = findValues
  val set = values.toSet
}
