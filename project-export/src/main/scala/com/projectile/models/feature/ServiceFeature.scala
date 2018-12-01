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

  case object GraphQL extends ServiceFeature(
    value = "graphql", title = "GraphQL", description = "Creates a Sangria GraphQL schema that exposes this service", dependsOn = Set(ProjectFeature.GraphQL)
  )

  override val values = findValues
  val set = values.toSet
}
