package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.project.Project

object ProjectStatusService {
  def status(p: Project) = {
    val root = File(p.getPath(OutputPath.Root))
    if (!root.exists) {
      Left("root-missing" -> "Root is missing")
    } else if (root.isEmpty) {
      Left("root-empty" -> "Root is empty")
    } else {
      Right("OK")
    }
  }

  def fix(p: Project, src: String, tgt: String) = src match {
    case "root-missing" | "root-empty" => buildExample(p, tgt)
  }

  private[this] def buildExample(p: Project, tgt: String) = {
    ProjectExampleService.extract(tgt, File(p.getPath(OutputPath.Root)), p.key)
  }
}
