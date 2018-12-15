package com.projectile.services.project

import com.projectile.models.input.Input
import com.projectile.models.project.member.ServiceMember
import com.projectile.services.ProjectileService
import com.projectile.util.JsonSerializers._

class ServiceMemberService(val svc: ProjectileService) {
  def saveServices(p: String, members: Seq[ServiceMember]) = {
    val proj = svc.getProjectSummary(p)
    val input = svc.getInput(proj.input)
    members.map(m => saveMember(p, input, m))
  }

  def removeService(p: String, member: String) = {
    val f = fileFor(p, member)
    f.delete(true)
    s"Removed service [$member]"
  }

  private[this] def fileFor(p: String, k: String) = svc.cfg.projectDir(p) / "service" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: ServiceMember) = {
    val m = i.exportService(member.key).apply(member)

    val f = fileFor(p, member.key)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(printJson(member.asJson))
    m
  }
}
