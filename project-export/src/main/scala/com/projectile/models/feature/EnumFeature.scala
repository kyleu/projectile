package com.projectile.models.feature

import enumeratum.values.{StringCirceEnum, StringEnum, StringEnumEntry}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.output.file.OutputFile

sealed abstract class EnumFeature(
    override val value: String,
    val title: String,
    val description: String,
    val dependsOn: Set[ProjectFeature] = Set(ProjectFeature.Core)
) extends StringEnumEntry

object EnumFeature extends StringEnum[EnumFeature] with StringCirceEnum[EnumFeature] {
  trait Logic {
    def export(config: ExportConfiguration, info: String => Unit, debug: String => Unit): Seq[OutputFile.Rendered]
  }

  case object Core extends EnumFeature(
    value = "core", title = "Core", description = "Enumeratum representation of this enum"
  )

  case object Shared extends EnumFeature(
    value = "shared", title = "Shared", description = "Store the file in the shared, rather than server, source directory"
  )

  case object GraphQL extends EnumFeature(
    value = "graphql", title = "GraphQL", description = "Sangria bindings for the enum as a scalar of field", dependsOn = Set(ProjectFeature.GraphQL)
  )

  case object Slick extends EnumFeature(
    value = "slick", title = "Slick", description = "Slick JDBC bindings for this enum", dependsOn = Set(ProjectFeature.Slick)
  )

  case object Doobie extends EnumFeature(
    value = "doobie", title = "Doobie", description = "Doobie JDBC bindings for this enum", dependsOn = Set(ProjectFeature.Doobie)
  )

  case object Controller extends EnumFeature(
    value = "controller", title = "Controller", description = "Play Framework Controller for this enum, why not", dependsOn = Set(ProjectFeature.Controller)
  )

  override val values = findValues
  val set = values.toSet
}
