package com.kyleu.projectile.models.thrift.input

import com.kyleu.projectile.models.export.ExportEnum
import com.kyleu.projectile.models.input.{Input, InputSummary, InputTemplate, InputType}
import com.kyleu.projectile.models.output.ExportHelper
import com.kyleu.projectile.models.thrift.schema._

object ThriftInput {
  def fromSummary(is: InputSummary, files: Seq[String]) = ThriftInput(key = is.key, description = is.description, files = files)
}

final case class ThriftInput(
    override val key: String = "new",
    override val description: String = "...",
    files: Seq[String] = Nil,
    typedefs: Map[String, String] = Map.empty,
    intEnums: Seq[ThriftIntEnum] = Nil,
    stringEnums: Seq[ThriftStringEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil,
    thriftUnions: Seq[ThriftUnion] = Nil,
    thriftServices: Seq[ThriftService] = Nil
) extends Input {
  override def template = InputTemplate.Thrift

  override def enum(key: String) = {
    getThriftEnum(key) match {
      case Left(ie) => ExportEnum(
        inputType = InputType.Enum.ThriftIntEnum,
        pkg = ie.pkg.toList :+ "models",
        key = ie.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(ie.key)),
        values = ie.values.map(v => ExportEnum.EnumVal(k = v._1, i = Some(v._2)))
      )
      case Right(se) => ExportEnum(
        inputType = InputType.Enum.ThriftStringEnum,
        pkg = se.pkg.toList :+ "models",
        key = se.key,
        className = ExportHelper.toClassName(ExportHelper.toIdentifier(se.key)),
        values = se.values.map(v => ExportEnum.EnumVal(v, s = Some(v)))
      )
    }
  }

  override lazy val enums = stringEnums.map(e => enum(e.key)) ++ intEnums.map(e => enum(e.key))
  override lazy val models = structs.map(s => ThriftExportModel.loadStructModel(s, this))
  override lazy val unions = thriftUnions.map(u => ThriftExportUnion.loadUnion(u, this))
  override lazy val services = thriftServices.map(s => ThriftExportService.loadService(s, this))

  lazy val exportModelNames = structs.map(_.key).toSet
  lazy val exportUnionNames = unions.map(_.key).toSet

  private[this] def getThriftEnum(k: String) = {
    intEnums.find(_.key == k).map(Left.apply).orElse(stringEnums.find(_.key == k).map(Right.apply)).getOrElse {
      val keys = (intEnums.map(_.key) ++ stringEnums.map(_.key)).sorted
      throw new IllegalStateException(s"Cannot find enum [$k] in input [$key] among candidates [${keys.mkString(", ")}]")
    }
  }
}
