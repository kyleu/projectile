package com.kyleu.projectile.services.input

import better.files.File
import com.kyleu.projectile.models.input.{InputSummary, InputTemplate}
import com.kyleu.projectile.models.thrift.input.{ThriftInput, ThriftOptions}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.thrift.ThriftParseService
import com.kyleu.projectile.util.{JacksonUtils, JsonFileLoader}
import com.kyleu.projectile.util.JsonSerializers._
import io.scalaland.chimney.dsl._

object ThriftInputService {
  private[this] val fn = "thrift-files.json"
  private[this] val refKey = "reference:"

  def saveThriftDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(JacksonUtils.printJackson(ThriftOptions().asJson))
  }

  def saveThrift(cfg: ConfigService, ti: ThriftInput) = {
    val summ = ti.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.Thrift).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val options = JacksonUtils.printJackson(ti.into[ThriftOptions].transform.asJson)
    (dir / fn).overwrite(options)

    ti
  }

  def loadThrift(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key

    val pc = JsonFileLoader.loadFile[ThriftOptions](dir / fn, "Thrift files")
    val files = pc.files.map {
      case x if x.startsWith(refKey) => (true, cfg.workingDirectory / x.stripPrefix(refKey))
      case x => (false, cfg.workingDirectory / x)
    }
    ThriftParseService.loadThriftInput(files, ThriftInput.fromSummary(summ, pc.files))
  }
}
