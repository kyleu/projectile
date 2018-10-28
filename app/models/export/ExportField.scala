package models.export

import models.database.schema.ColumnType
import models.database.schema.ColumnType._
import models.export.config.ExportConfiguration
import models.output.ExportHelper
import models.output.file.ScalaFile
import util.JsonSerializers._

object ExportField {
  implicit val jsonEncoder: Encoder[ExportField] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportField] = deriveDecoder

  def getDefaultString(t: ColumnType, enumOpt: Option[ExportEnum], defaultValue: Option[String]) = t match {
    case BooleanType => defaultValue.map(v => if (v == "1" || v == "true") { "true" } else { "false" }).getOrElse("false")
    case ByteType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0")
    case IntegerType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0")
    case LongType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0") + "L"
    case ShortType => defaultValue.filter(_.matches("[0-9]+")).getOrElse("0") + ".toShort"
    case FloatType => defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0.0") + "f"
    case DoubleType => defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0.0")
    case BigDecimalType => s"BigDecimal(${defaultValue.filter(_.matches("[0-9\\.]+")).getOrElse("0")})"

    case DateType => "DateUtils.today"
    case TimeType => "DateUtils.currentTime"
    case TimestampType => "DateUtils.now"
    case TimestampZonedType => "DateUtils.nowZoned"

    case UuidType => defaultValue.filter(_.length == 36).map(d => s"""UUID.fromString("$d")""").getOrElse("UUID.randomUUID")

    case JsonType => "Json.obj()"
    case ArrayType => "List.empty"
    case TagsType => s"List.empty[Tag]"
    case EnumType => enumOpt match {
      case Some(enum) =>
        val (_, cn) = defaultValue.flatMap(d => enum.valuesWithClassNames.find(_._1 == d)).getOrElse {
          enum.valuesWithClassNames.headOption.getOrElse(throw new IllegalStateException(s"No enum values for [${enum.name}]."))
        }
        s"${enum.className}.$cn"
      case None => "\"" + defaultValue.getOrElse("") + "\""
    }
    case _ => "\"" + defaultValue.getOrElse("") + "\""
  }
}

case class ExportField(
    columnName: String,
    propertyName: String,
    title: String,
    fkNameOverride: String = "",
    description: Option[String],
    idx: Int = 0,
    t: ColumnType,
    sqlTypeName: String,
    defaultValue: Option[String] = None,
    notNull: Boolean = false,
    indexed: Boolean = false,
    unique: Boolean = false,
    inSearch: Boolean = false,
    inView: Boolean = true,
    inSummary: Boolean = false,
    ignored: Boolean = false
) {
  val nullable = !notNull

  val className = ExportHelper.toClassName(propertyName)

  def classNameForSqlType(config: ExportConfiguration) = t match {
    case EnumType => enumOpt(config).map { e =>
      s"EnumType(${e.className})"
    }.getOrElse(throw new IllegalStateException(s"Cannot find enum matching [$sqlTypeName]."))
    case ArrayType => ArrayType.typForSqlType(sqlTypeName)
    case _ => t.className
  }

  def enumOpt(config: ExportConfiguration) = t match {
    case ColumnType.EnumType => config.getEnumOpt(sqlTypeName)
    case _ => None
  }

  def scalaType(config: ExportConfiguration) = enumOpt(config).map(_.className).getOrElse(t.asScala)

  def addImport(config: ExportConfiguration, file: ScalaFile, pkg: Seq[String]) = {
    enumOpt(config) match {
      case Some(enum) if enum.modelPackage == pkg => // Noop
      case Some(enum) => file.addImport((config.applicationPackage ++ enum.modelPackage).mkString("."), enum.className)
      case None => t.requiredImport.foreach(pkg => file.addImport(pkg, t.asScala))
    }
  }

  def defaultString(config: ExportConfiguration) = ExportField.getDefaultString(t, enumOpt(config), defaultValue)

  def fromString(config: ExportConfiguration, s: String) = enumOpt(config).map { enum =>
    s"${enum.className}.withValue($s)"
  }.getOrElse(t.fromString.replaceAllLiterally("xxx", s))
}
