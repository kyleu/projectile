package com.projectile.models.feature.graphql.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.typ.FieldType._
import com.projectile.models.export.typ.{FieldType, FieldTypeAsScala}
import com.projectile.models.output.file.ScalaFile

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

    case ObjectType => "StringType"
    case StructType(key) => config.getModel(key).propertyName + "InputType"
    case EnumType(key) => config.getEnum(key).propertyName + "InputType"

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
          case Some(e) => file.addImport(e.pkg :+ "graphql" :+ s"${impType}Schema", s"${e.propertyName}Type")
          case None => config.models.find(_.key == impType) match {
            case Some(m) => file.addImport(m.pkg :+ "graphql" :+ s"${impType}Schema", s"${m.propertyName}Type")
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
          case Some(m) => file.addImport(m.pkg :+ "graphql" :+ s"${impType}Schema", s"${m.propertyName}InputType")
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
