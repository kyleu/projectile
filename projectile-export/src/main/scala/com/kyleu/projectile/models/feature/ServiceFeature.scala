package com.kyleu.projectile.models.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.output.file.OutputFile

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

  case object Controller extends ServiceFeature(
    value = "controller", title = "Controller", description = "Creates a Play Framework controller for this service", dependsOn = Set(ProjectFeature.Controller)
  )

  case object Auth extends ServiceFeature(
    value = "auth", title = "Auth", description = "Weaves credentials through service and controller calls", dependsOn = Set(ProjectFeature.Service)
  )

  override val values = findValues
  val set = values.toSet
}
