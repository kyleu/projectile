package com.projectile.models.feature.thrift

import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.export.{ExportField, ExportModel}
import com.projectile.models.output.file.ThriftFile

object ThriftServiceFile {
  private[this] val credsParam = "1: common.Credentials creds,"

  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = ThriftFile("services" +: model.pkg, model.className + "Service")

    file.add("namespace java " + (config.applicationPackage ++ Seq("services") ++ model.pkg).mkString("."))
    file.add()

    val parent = ("services" +: model.pkg).map(_ => "../").mkString
    file.add(s"""include "${parent}common.thrift"""")
    file.add(s"""include "${parent}result.thrift"""")
    file.add(s"""include "../../${("models" +: model.pkg).mkString("/")}/${model.className}.thrift"""")
    file.add()

    file.add(s"service ${model.className}Service {", 1)

    val retType = s"${model.className}.${model.className}"

    writePk(config, file, model.pkFields, retType)
    file.add()
    writeGetAll(file, retType)
    file.add()
    writeSearch(file, retType)
    file.add()
    ThriftServiceRelations.writeRelations(file)
    file.add()
    ThriftServiceMutations.writeMutations(config, file, model.pkFields, retType)

    file.add("}", -1)

    file
  }

  private[this] def writePk(config: ExportConfiguration, file: ThriftFile, pkFields: List[ExportField], retType: String) = pkFields match {
    case Nil => // noop
    case pkf :: Nil =>
      val thriftType = ExportFieldThrift.thriftType(pkf.t, pkf.sqlTypeName, pkf.enumOpt(config))
      val thriftVisibility = if (pkf.notNull) { "required" } else { "optional" }

      file.add(s"$retType getByPrimaryKey(", 1)
      file.add(credsParam)
      file.add(s"2: $thriftVisibility $thriftType ${pkf.propertyName}")
      file.add(")", -1)
      file.add(s"$retType getByPrimaryKeySeq(", 1)
      file.add(credsParam)
      file.add(s"2: $thriftVisibility list<$thriftType> ${pkf.propertyName}")
      file.add(")", -1)
    case _ => // Noop
  }

  private[this] def writeGetAll(file: ThriftFile, retType: String) = {
    file.add("common.int countAll(", 1)
    file.add(credsParam)
    file.add("2: list<result.Filter> filters")
    file.add(")", -1)
    file.add(s"list<$retType> getAll(", 1)
    file.add(credsParam)
    file.add("2: list<result.Filter> filters,")
    file.add("3: list<result.OrderBy> orderBys,")
    file.add("4: common.int limit,")
    file.add("5: common.int offset")
    file.add(")", -1)
  }

  private[this] def writeSearch(file: ThriftFile, retType: String) = {
    file.add("common.int searchCount(", 1)
    file.add(credsParam)
    file.add("2: required string q,")
    file.add("3: list<result.Filter> filters")
    file.add(")", -1)
    file.add(s"list<$retType> search(", 1)
    file.add(credsParam)
    file.add("2: required string q,")
    file.add("3: list<result.Filter> filters,")
    file.add("4: list<result.OrderBy> orderBys,")
    file.add("5: common.int limit,")
    file.add("6: common.int offset")
    file.add(")", -1)
    file.add(s"list<$retType> searchExact(", 1)
    file.add(credsParam)
    file.add("2: required string q,")
    file.add("3: list<result.OrderBy> orderBys,")
    file.add("4: common.int limit,")
    file.add("5: common.int offset")
    file.add(")", -1)
  }
}
