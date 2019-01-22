package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.project.member.EnumMember
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.util.JacksonUtils
import com.kyleu.projectile.util.JsonSerializers._

class EnumMemberService(val svc: ProjectileService) {
  def saveEnums(p: String, members: Seq[EnumMember]) = {
    val input = svc.getInput(svc.getProjectSummary(p).input)
    members.map(m => saveMember(p, input, m))
  }

  def removeEnum(p: String, member: String) = {
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member)
    f.delete(true)
    s"Removed enum [$member]"
  }

  private[this] def fileFor(dir: File, k: String) = dir / "enum" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: EnumMember) = {
    val o = i.exportEnum(member.key)
    val m = o.apply(member)
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member.key)
    f.createFileIfNotExists(createParents = true)
    val content = JacksonUtils.printJackson(member.asJson)
    f.overwrite(content)
    m
  }
}
