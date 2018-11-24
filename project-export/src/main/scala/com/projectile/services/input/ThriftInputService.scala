package com.projectile.services.input

import better.files.File
import com.projectile.models.thrift.schema.{ThriftEnum, ThriftService, ThriftStruct}
import com.projectile.models.input.{InputSummary, InputTemplate}
import com.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.projectile.services.config.ConfigService
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

    if (ti.intEnums.nonEmpty) {
      val enumDir = dir / "intEnum"
      enumDir.createDirectories()
      ti.intEnums.foreach(e => (enumDir / s"${e.key}.json").overwrite(e.asJson.spaces2))
    }

    if (ti.intEnums.nonEmpty) {
      val enumDir = dir / "stringEnum"
      enumDir.createDirectories()
      ti.stringEnums.foreach(e => (enumDir / s"${e.key}.json").overwrite(e.asJson.spaces2))
    }

    if (ti.structs.nonEmpty) {
      val structDir = dir / "struct"
      structDir.createDirectories()
      ti.structs.foreach(t => (structDir / s"${t.key}.json").overwrite(t.asJson.spaces2))
    }

    if (ti.services.nonEmpty) {
      val viewDir = dir / "service"
      viewDir.createDirectories()
      ti.services.foreach(v => (viewDir / s"${v.key}.json").overwrite(v.asJson.spaces2))
    }

    ti
  }

  def toThriftInput(
    summ: InputSummary, to: ThriftOptions, stringEnums: Seq[ThriftEnum] = Nil, intEnums: Seq[ThriftEnum] = Nil,
    structs: Seq[ThriftStruct] = Nil, services: Seq[ThriftService] = Nil
  ) = {
    summ.into[ThriftInput]
      .withFieldComputed(_.files, _ => to.files)
      .withFieldComputed(_.intEnums, _ => intEnums).withFieldComputed(_.stringEnums, _ => stringEnums)
      .withFieldComputed(_.structs, _ => structs).withFieldComputed(_.services, _ => services)
      .transform
  }

  def loadThrift(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = loadFile[ThriftOptions](dir / fn, "Thrift connection")

    def loadDir[A: Decoder](k: String) = {
      val d = dir / k
      if (d.exists && d.isDirectory && d.isReadable) {
        d.children.map(f => loadFile[A](f, k)).toList
      } else {
        Nil
      }
    }

    toThriftInput(summ, pc, loadDir[ThriftEnum]("stringEnum"), loadDir[ThriftEnum]("intEnum"), loadDir[ThriftStruct]("table"), loadDir[ThriftService]("view"))
  }
}
