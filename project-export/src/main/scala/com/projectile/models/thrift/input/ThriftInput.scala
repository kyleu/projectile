package com.projectile.models.thrift.input

import com.projectile.models.export.ExportEnum
import com.projectile.models.input.{Input, InputTemplate}
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.EnumMember.InputType
import com.projectile.models.thrift.schema.{ThriftEnum, ThriftService, ThriftStruct}
import com.projectile.util.JsonSerializers._

object ThriftInput {
  implicit val jsonEncoder: Encoder[ThriftInput] = deriveEncoder
  implicit val jsonDecoder: Decoder[ThriftInput] = deriveDecoder
}

case class ThriftInput(
    override val key: String = "new",
    override val title: String = "New Thrift Imput",
    override val description: String = "...",
    files: Seq[String] = Nil,
    intEnums: Seq[ThriftEnum] = Nil,
    stringEnums: Seq[ThriftEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil,
    services: Seq[ThriftService] = Nil
) extends Input {
  override def template = InputTemplate.Thrift

  def getEnum(k: String) = intEnums.find(_.key == k).orElse(stringEnums.find(_.key == k)).getOrElse {
    throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${(intEnums ++ stringEnums).map(_.key).mkString(", ")}]")
  }

  def getIntEnum(k: String) = intEnums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find int enum [$k] in input [$key] among candidates [${intEnums.map(_.key).mkString(", ")}]")
  }

  def getStringEnum(k: String) = stringEnums.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find string enum [$k] in input [$key] among candidates [${stringEnums.map(_.key).mkString(", ")}]")
  }

  def getStruct(k: String) = structs.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find struct [$k] in input [$key] among candidates [${structs.map(_.key).mkString(", ")}]")
  }

  def getService(k: String) = services.find(_.key == k).getOrElse {
    throw new IllegalStateException(s"Cannot find service [$k] in input [$key] among candidates [${services.map(_.key).mkString(", ")}]")
  }

  override def exportEnum(key: String) = {
    val e = getEnum(key)
    ExportEnum(inputType = InputType.ThriftEnum, key = e.key, className = ExportHelper.toClassName(ExportHelper.toIdentifier(e.key)), values = e.values)
  }

  override lazy val exportEnums = (stringEnums ++ intEnums).map(e => exportEnum(e.key))

  override def exportModel(key: String) = {
    structs.find(_.key == key) match {
      case Some(struct) => ThriftExportModel.loadStructModel(struct, structs, exportEnums)
      case None => throw new IllegalStateException(s"Cannot find struct [$key] in input [$key]")
    }
  }

  override lazy val exportModels = structs.map(e => exportModel(e.key))
}
