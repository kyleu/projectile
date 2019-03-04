package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.project.member.ServiceMember
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.util.JacksonUtils
import com.kyleu.projectile.util.JsonSerializers._

class ServiceMemberService(val svc: ProjectileService) {
  def saveServices(p: String, members: Seq[ServiceMember]) = {
    val input = svc.getInput(svc.getProjectSummary(p).input)
    members.map(m => saveMember(p, input, m))
  }

  def removeService(p: String, member: String) = {
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member)
    f.delete(true)
    s"Removed service [$member]"
  }

  private[this] def fileFor(dir: File, k: String) = dir / "service" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: ServiceMember) = {
    val o = i.service(member.key)
    val m = o.apply(member)
    if (o != m) {
      val dir = svc.configForProject(p).projectDir(p)
      val f = fileFor(dir, member.key)
      f.createFileIfNotExists(createParents = true)
      f.overwrite(JacksonUtils.printJackson(member.asJson))
    }
    m
  }
}
