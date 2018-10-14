package services.output

import models.output.file.OutputFile
import services.config.ConfigService

class OutputService(cfg: ConfigService) {
  val buildDir = cfg.buildDirectory / "build"

  def clean(key: Option[String]) = key match {
    case Some(k) => (buildDir / k).delete(swallowIOExceptions = true)
    case None => buildDir.delete(swallowIOExceptions = true)
  }

  def dirForProjectPath(key: String, path: OutputPath) = {
    val ret = buildDir / key / path.value
    if (!ret.isDirectory) {
      ret.createDirectories()
    }
    ret
  }

  def write(key: String, file: OutputFile.Rendered) = {
    val f = dirForProjectPath(key, file.path) / file.dir.mkString("/") / file.filename
    f.createFileIfNotExists(createParents = true)
    f.overwrite(file.content)
    f
  }
}