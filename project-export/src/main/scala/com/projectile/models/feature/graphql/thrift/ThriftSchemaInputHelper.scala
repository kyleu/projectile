package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.projectile.models.output.ExportHelper
import com.projectile.models.output.file.ScalaFile

object ThriftSchemaInputHelper {
  private[this] def getImportType(config: ExportConfiguration, t: FieldType): Option[String] = t match {
    case _ if FieldType.scalars(t) => None
    case FieldType.MapType(_, v) => getImportType(config, v)
    case FieldType.ListType(typ) => getImportType(config, typ)
    case FieldType.SetType(typ) => getImportType(config, typ)
    case _ => Some(FieldTypeAsScala.asScala(config, t))
  }

  def addInputImports(pkg: Seq[String], types: Seq[FieldType], config: ExportConfiguration, file: ScalaFile) = {
    types.foreach { colType =>
      getImportType(config, colType).foreach { impType =>
        config.enums.find(_.key == impType) match {
          case Some(e) => file.addImport(e.pkg :+ "graphql" :+ s"${impType}Schema", s"${ExportHelper.toIdentifier(impType)}Type")
          case None =>
        }
      }
    }
  }
}
