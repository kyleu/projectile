package com.projectile.services.input

import better.files.File
import com.projectile.models.thrift.schema._
import com.projectile.models.input.{InputSummary, InputTemplate}
import com.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.projectile.services.config.ConfigService
import com.projectile.services.thrift.ThriftParseService
import com.projectile.util.JsonSerializers._
import io.scalaland.chimney.dsl._

object ThriftInputService {
  private[this] val fn = "thrift-files.json"

  def saveThriftDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(ThriftOptions().asJson.spaces2)
  }

  def saveThrift(cfg: ConfigService, ti: ThriftInput) = {
    val summ = ti.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.Thrift).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val options = ti.into[ThriftOptions].transform.asJson.spaces2
    (dir / fn).overwrite(options)

    ti
  }

  def toThriftInput(
    summ: InputSummary, to: ThriftOptions, typedefs: Map[String, String] = Map.empty[String, String],
    intEnums: Seq[ThriftIntEnum] = Nil, stringEnums: Seq[ThriftStringEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil, services: Seq[ThriftService] = Nil
  ) = {
    summ.into[ThriftInput]
      .withFieldComputed(_.files, _ => to.files)
      .withFieldComputed(_.typedefs, _ => typedefs)
      .withFieldComputed(_.intEnums, _ => intEnums).withFieldComputed(_.stringEnums, _ => stringEnums)
      .withFieldComputed(_.structs, _ => structs).withFieldComputed(_.services, _ => services)
      .transform
  }

  def loadThrift(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = loadFile[ThriftOptions](dir / fn, "Thrift connection")

    val results = ThriftParseService.loadFiles(pc.files.map(cfg.workingDirectory / _))

    toThriftInput(
      summ = summ,
      to = pc,
      typedefs = results.flatMap(_.typedefs).toMap,
      intEnums = results.flatMap(_.intEnums),
      stringEnums = results.flatMap(_.stringEnums),
      structs = results.flatMap(_.structs),
      services = results.flatMap(_.services)
    )
  }
}
