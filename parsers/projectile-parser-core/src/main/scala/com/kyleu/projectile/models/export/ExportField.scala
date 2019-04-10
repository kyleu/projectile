package com.kyleu.projectile.models.export

import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.export.typ.{FieldType, FieldTypeAsScala, FieldTypeImports}
import com.kyleu.projectile.models.export.typ.FieldType._
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.output.file.ScalaFile
import com.kyleu.projectile.util.JsonSerializers._

object ExportField {
  implicit val jsonEncoder: Encoder[ExportField] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportField] = deriveDecoder

  def getDefaultString(config: ExportConfiguration, t: FieldType, defaultValue: Option[String]) = t match {
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

    case _: ListType => "List.empty"
    case EnumType(key) =>
      val enum = config.getEnum(key, "field default")
      s"${enum.className}.${defaultValue.flatMap(d => enum.values.find(_.k == d)).getOrElse(enum.firstVal).className}"

    case JsonType => "Json.obj()"
    case TagsType => "List.empty[Tag]"

    case _ => "\"" + defaultValue.getOrElse("") + "\""
  }
}

case class ExportField(
    key: String,
    propertyName: String,
    title: String,
    description: Option[String],
    t: FieldType,
    defaultValue: Option[String] = None,
    required: Boolean = false,
    indexed: Boolean = false,
    unique: Boolean = false,
    inSearch: Boolean = false,
    inView: Boolean = true,
    inSummary: Boolean = false,
    ignored: Boolean = false
) {
  val optional = !required

  val className = ExportHelper.toClassName(propertyName)

  def scalaType(config: ExportConfiguration) = FieldTypeAsScala.asScala(config, t)
  def scalaTypeFull(config: ExportConfiguration) = FieldTypeImports.imports(config, t).headOption.getOrElse(Seq(scalaType(config)))

  def addImport(config: ExportConfiguration, file: ScalaFile, pkg: Seq[String], isJs: Boolean = false, isThrift: Boolean = false) = {
    FieldTypeImports.imports(config = config, t = t, isJs = isJs, isThrift = isThrift).foreach { pkg =>
      file.addImport(pkg.init, pkg.lastOption.getOrElse(throw new IllegalStateException()))
    }
  }

  def defaultString(config: ExportConfiguration) = ExportField.getDefaultString(config, t, defaultValue)
}
