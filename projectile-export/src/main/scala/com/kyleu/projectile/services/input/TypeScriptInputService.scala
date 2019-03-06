package com.kyleu.projectile.services.input

import better.files.File
import com.kyleu.projectile.models.input.{InputSummary, InputTemplate}
import com.kyleu.projectile.models.typescript.input.{TypeScriptInput, TypeScriptOptions}
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.services.typescript.FileService
import com.kyleu.projectile.util.JsonSerializers._
import com.kyleu.projectile.util.{JacksonUtils, JsonFileLoader}
import io.scalaland.chimney.dsl._

object TypeScriptInputService {
  private[this] val fn = "typescript-files.json"

  def saveTypeScriptDefault(cfg: ConfigService, dir: File) = if (!(dir / fn).exists) {
    (dir / fn).overwrite(JacksonUtils.printJackson(TypeScriptOptions().asJson))
  }

  def saveTypeScript(cfg: ConfigService, ti: TypeScriptInput) = {
    val summ = ti.into[InputSummary].withFieldComputed(_.template, _ => InputTemplate.TypeScript).transform
    val dir = SummaryInputService.saveSummary(cfg, summ)

    val options = JacksonUtils.printJackson(ti.into[TypeScriptOptions].transform.asJson)
    (dir / fn).overwrite(options)

    ti
  }

  def loadTypeScript(cfg: ConfigService, summ: InputSummary) = {
    val dir = cfg.inputDirectory / summ.key
    val opts = JsonFileLoader.loadFile[TypeScriptOptions](dir / fn, "TypeScript files")
    inputFor(cfg = cfg, key = summ.key, desc = summ.description, files = opts.files)
  }

  def inputFor(cfg: ConfigService, key: String, desc: String, files: Seq[String]) = FileService.loadTypeScriptInput(
    root = cfg.workingDirectory,
    cache = cfg.configDirectory / ".cache" / "typescript",
    files = files,
    input = TypeScriptInput.fromSummary(InputSummary(template = InputTemplate.TypeScript, key = key, description = desc), files)
  )
}
