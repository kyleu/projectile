package com.projectile.services.input

import better.files.File
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

  def loadThrift(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = loadFile[ThriftOptions](dir / fn, "Thrift connection")
    val files = pc.files.map(cfg.workingDirectory / _)
    ThriftParseService.loadThriftInput(files, ThriftInput.fromSummary(summ, pc.files))
  }
}
