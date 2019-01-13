package com.kyleu.projectile.services.project.codegen

import com.kyleu.projectile.models.project.codegen.CodegenResult
import com.kyleu.projectile.services.ProjectileService

trait CodegenHelper { this: ProjectileService =>
  def getProjectHash(project: String) = {
    val f = configForProject(project).projectDirectory / project / "inputHash.json"
    if (f.exists && f.isRegularFile) { f.contentAsString.toInt } else { 0 }
  }

  def setProjectHash(project: String, hash: Int) = {
    val f = configForProject(project).projectDirectory / project / "inputHash.json"
    f.overwrite(hash.toString)
  }

  def codegen(projectKeys: Seq[String] = Nil, verbose: Boolean) = {
    val startMs = System.currentTimeMillis
    val projects = listProjects().filter(p => if (projectKeys.isEmpty) { true } else { projectKeys.contains(p.key) })

    val workingSet = projects.map(p => (p, getProjectHash(p.key), getInput(p.input)))

    val updates = workingSet.flatMap {
      case (_, h, i) if h == i.hash => None
      case (p, _, _) => updateProject(p.key)
    }

    val ret = workingSet.flatMap {
      case (_, h, i) if h == i.hash => None
      case (p, _, i) => Some(exportProject(key = p.key, verbose = verbose))
    }

    val aud = if (ret.isEmpty) {
      None
    } else {
      Some(auditKeys(ret.map(_._1.project.key), verbose = verbose))
    }

    CodegenResult(updates, ret, aud, (System.currentTimeMillis - startMs).toInt)
  }
}
