package com.kyleu.projectile.services.project

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
    val f = fileFor(p, member)
    f.delete(true)
    s"Removed model [$member]"
  }

  private[this] def fileFor(p: String, k: String) = svc.cfg.projectDir(p) / "model" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: ModelMember) = {
    val m = i.exportModel(member.key).apply(member)

    val f = fileFor(p, member.key)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(printJson(member.asJson))
    m
  }
}
