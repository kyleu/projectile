package com.projectile.models.thrift.input

import com.projectile.models.export.ExportEnum
import com.projectile.models.input.{Input, InputSummary, InputTemplate}
import com.projectile.models.output.ExportHelper
import com.projectile.models.project.member.EnumMember.InputType
import com.projectile.models.thrift.schema.{ThriftIntEnum, ThriftService, ThriftStringEnum, ThriftStruct}

object ThriftInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = ThriftInput(key = is.key, title = is.title, description = is.description, files = files)
}

case class ThriftInput(
    override val key: String = "new",
    override val title: String = "New Thrift Imput",
    override val description: String = "...",
    files: Seq[String] = Nil,
    typedefs: Map[String, String] = Map.empty,
    intEnums: Seq[ThriftIntEnum] = Nil,
    stringEnums: Seq[ThriftStringEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil,
    services: Seq[ThriftService] = Nil
) extends Input {
  override def template = InputTemplate.Thrift

  def getEnum(k: String) = {
    intEnums.find(_.key == k).map(Left.apply).orElse(stringEnums.find(_.key == k).map(Right.apply)).getOrElse {
      val keys = (intEnums.map(_.key) ++ stringEnums.map(_.key)).sorted
      throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${keys.mkString(", ")}]")
    }
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
    getEnum(key) match {
      case Left(ie) => ExportEnum(
        inputType = InputType.ThriftIntEnum,
        pkg = ie.pkg.toList,
        key = ie.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(ie.key)),
        values = ie.values.map(v => v._2 + ":" + v._1)
      )
      case Right(se) => ExportEnum(
        inputType = InputType.ThriftStringEnum,
        pkg = se.pkg.toList,
        key = se.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(se.key)),
        values = se.values
      )
    }
  }

  override lazy val exportEnums = stringEnums.map(e => exportEnum(e.key)) ++ intEnums.map(e => exportEnum(e.key))

  override def exportModel(key: String) = {
    structs.find(_.key == key) match {
      case Some(struct) => ThriftExportModel.loadStructModel(struct, structs, exportEnums)
      case None => throw new IllegalStateException(s"Cannot find struct [$key] in input [$key]")
    }
  }

  override lazy val exportModels = structs.map(e => exportModel(e.key))
}
