package services.project

import models.input.Input
import models.project.member.EnumMember
import services.ProjectileService
import util.JsonSerializers._

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
    val content = member.asJson.spaces2
    f.overwrite(content)
    m
  }
}
