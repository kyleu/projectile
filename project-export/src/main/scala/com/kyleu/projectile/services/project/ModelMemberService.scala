package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.project.member.ModelMember
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.util.JsonSerializers._

class ModelMemberService(val svc: ProjectileService) {
  def saveModels(p: String, members: Seq[ModelMember]) = {
    val input = svc.getInput(svc.getProjectSummary(p).input)
    members.map(m => saveMember(p, input, m))
  }

  def removeModel(p: String, member: String) = {
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member)
    f.delete(true)
    s"Removed model [$member]"
  }

  private[this] def fileFor(dir: File, k: String) = dir / "model" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: ModelMember) = {
    val m = i.exportModel(member.key).apply(member)
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member.key)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(printJson(member.asJson))
    m
  }
}
