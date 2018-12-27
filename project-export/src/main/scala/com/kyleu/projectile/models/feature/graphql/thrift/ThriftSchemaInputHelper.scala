package com.kyleu.projectile.models.feature.graphql.thrift

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.kyleu.projectile.models.output.file.ScalaFile

object ThriftSchemaInputHelper {
  def graphQlInputTypeFor(t: FieldType, config: ExportConfiguration): String = t match {
    case UnitType => "UnitType"

    case StringType => "StringType"
    case EncryptedStringType => "StringType"

    case BooleanType => "BooleanType"
    case ByteType => "byteType"
    case ShortType => "IntType"
    case IntegerType => "IntType"
    case LongType => "LongType"
    case FloatType => "FloatType"
    case DoubleType => "DoubleType"
    case BigDecimalType => "BigDecimalType"

    case DateType => "localDateType"
    case TimeType => "localTimeType"
    case TimestampType => "localDateTimeType"
    case TimestampZonedType => "zonedDateTimeType"

    case RefType => "StringType"
    case XmlType => "StringType"
    case UuidType => "uuidType"

    case EnumType(key) => config.getEnum(key, "graphql thrift").propertyName + "InputType"
    case StructType(key) => config.getModel(key, "graphql thrift").propertyName + "InputType"
    case ObjectType(_, _) => throw new IllegalStateException("Object types are not supported in Thrift")

    case ListType(typ) => s"ListInputType(${graphQlInputTypeFor(typ, config)})"
    case SetType(typ) => s"ListInputType(${graphQlInputTypeFor(typ, config)})"
    case MapType(_, _) => "StringType"

    case JsonType => "StringType"
    case CodeType => "StringType"
    case TagsType => "TagsType"
    case ByteArrayType => "ArrayType(StringType)"
  }

  def addImports(pkg: Seq[String], types: Seq[FieldType], config: ExportConfiguration, file: ScalaFile) = {
    types.foreach { colType =>
      getImportType(config, colType).foreach { impType =>
        config.enums.find(_.key == impType) match {
          case Some(e) => file.addImport(e.pkg :+ s"${impType}Schema", s"${e.propertyName}EnumType")
          case None => config.models.find(_.key == impType) match {
            case Some(m) => file.addImport(m.pkg :+ s"${impType}Schema", s"${m.propertyName}Type")
            case None => // noop?
          }
        }
      }
    }
  }

  def addInputImports(pkg: Seq[String], types: Seq[FieldType], config: ExportConfiguration, file: ScalaFile) = {
    types.foreach { colType =>
      getImportType(config, colType).foreach { impType =>
        config.models.find(_.key == impType) match {
          case Some(m) => file.addImport(m.pkg :+ s"${impType}Schema", s"${m.propertyName}InputType")
          case None => // noop?
        }
      }
    }
  }

  private[this] def getImportType(config: ExportConfiguration, t: FieldType): Option[String] = t match {
    case _ if FieldType.scalars(t) => None
    case FieldType.MapType(_, v) => getImportType(config, v)
    case FieldType.ListType(typ) => getImportType(config, typ)
    case FieldType.SetType(typ) => getImportType(config, typ)
    case _ => Some(FieldTypeAsScala.asScala(config, t))
  }
}
