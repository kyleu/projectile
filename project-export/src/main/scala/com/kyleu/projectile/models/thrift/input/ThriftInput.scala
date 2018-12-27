package com.kyleu.projectile.models.thrift.input

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.input.{InputType, Input, InputSummary, InputTemplate}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.thrift.schema.{ThriftIntEnum, ThriftService, ThriftStringEnum, ThriftStruct}

object ThriftInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = ThriftInput(key = is.key, description = is.description, files = files)
}

case class ThriftInput(
    override val key: String = "new",
    override val description: String = "...",
    files: Seq[String] = Nil,
    typedefs: Map[String, String] = Map.empty,
    intEnums: Seq[ThriftIntEnum] = Nil,
    stringEnums: Seq[ThriftStringEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil,
    services: Seq[ThriftService] = Nil
) extends Input {
  override def template = InputTemplate.Thrift

  override def exportEnum(key: String) = {
    getThriftEnum(key) match {
      case Left(ie) => ExportEnum(
        inputType = InputType.Enum.ThriftIntEnum,
        pkg = ie.pkg.toList :+ "models",
        key = ie.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(ie.key)),
        values = ie.values.map(v => v._2 + ":" + v._1)
      )
      case Right(se) => ExportEnum(
        inputType = InputType.Enum.ThriftStringEnum,
        pkg = se.pkg.toList :+ "models",
        key = se.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(se.key)),
        values = se.values
      )
    }
  }

  override lazy val exportEnums = stringEnums.map(e => exportEnum(e.key)) ++ intEnums.map(e => exportEnum(e.key))

  lazy val exportModelNames = structs.map(_.key).toSet

  override def exportModel(k: String) = structs.find(_.key == k) match {
    case Some(struct) => ThriftExportModel.loadStructModel(struct, this)
    case None => throw new IllegalStateException(s"Cannot find struct [$k] in input [$key]")
  }

  override lazy val exportModels = structs.map(e => exportModel(e.key))

  override def exportService(k: String) = services.find(_.key == k) match {
    case Some(svc) => ThriftExportService.loadService(svc, this)
    case None => throw new IllegalStateException(s"Cannot find service [$k] in input [$key]")
  }

  override def exportServices = services.map(s => exportService(s.key))

  private[this] def getThriftEnum(k: String) = {
    intEnums.find(_.key == k).map(Left.apply).orElse(stringEnums.find(_.key == k).map(Right.apply)).getOrElse {
      val keys = (intEnums.map(_.key) ++ stringEnums.map(_.key)).sorted
      throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${keys.mkString(", ")}]")
    }
  }
}
