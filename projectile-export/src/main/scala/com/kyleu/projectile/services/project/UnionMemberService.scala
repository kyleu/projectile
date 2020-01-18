package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.project.member.UnionMember
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.util.JacksonUtils
import com.kyleu.projectile.util.JsonSerializers._

class UnionMemberService(val svc: ProjectileService) {
  def saveUnions(p: String, members: Seq[UnionMember]) = {
    val input = svc.getInput(svc.getProjectSummary(p).input)
    members.map(u => saveMember(p, input, u))
  }

  def removeUnion(p: String, member: String) = {
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member)
    f.delete(true)
    s"Removed union [$member]"
  }

  private[this] def fileFor(dir: File, k: String) = dir / "union" / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: UnionMember) = {
    val o = i.union(member.key)
    val m = o.apply(member)
    val dir = svc.configForProject(p).projectDir(p)
    val f = fileFor(dir, member.key)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(JacksonUtils.printJackson(member.asJson))
    m
  }
}
