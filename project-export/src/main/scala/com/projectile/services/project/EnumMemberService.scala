package com.projectile.services.project

import com.projectile.models.input.Input
import com.projectile.models.project.member.EnumMember
import com.projectile.services.ProjectileService
import com.projectile.util.JsonSerializers._

class EnumMemberService(val svc: ProjectileService) {
  def saveEnums(p: String, members: Seq[EnumMember]) = {
    val inputs = members.map(_.input).distinct.map(i => i -> svc.getInput(i)).toMap
    members.map(m => saveMember(p, inputs(m.input), m))
  }

  def removeEnum(p: String, member: String) = {
    val f = fileFor(p, member)
    f.delete(true)
    s"Removed enum [$member]"
  }

  private[this] def fileFor(p: String, k: String) = svc.cfg.projectDir(p) / "enum" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: EnumMember) = {
    val o = i.exportEnum(member.key)
    val m = o.apply(member)

    val f = fileFor(p, member.key)
    f.createFileIfNotExists(createParents = true)
    val content = printJson(member.asJson)
    f.overwrite(content)
    m
  }
}
