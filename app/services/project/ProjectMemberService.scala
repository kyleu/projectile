package services.project

import models.input.Input
import models.project.member.ProjectMember
import services.ProjectileService
import util.JsonSerializers._

class ProjectMemberService(val svc: ProjectileService) {
  def save(p: String, members: Seq[ProjectMember]) = {
    val inputs = members.map(_.input).distinct.map(i => i -> svc.getInput(i)).toMap
    members.map(m => saveMember(p, inputs(m.input), m))
  }

  def remove(p: String, t: ProjectMember.OutputType, member: String) = {
    val f = fileFor(p, t, member)
    f.delete(true)
    s"Removed $t [$member]"
  }

  private[this] def fileFor(p: String, t: ProjectMember.OutputType, k: String) = svc.cfg.projectDir(p) / t.value / (k + ".json")

  private[this] def saveMember(p: String, i: Input, member: ProjectMember) = {
    val m = member.inputType.out match {
      case ProjectMember.OutputType.Model => i.exportModel(member.key).apply(member)
      case ProjectMember.OutputType.Enum => i.exportEnum(member.key).apply(member)
    }

    val f = fileFor(p, member.outputType, member.key)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(member.asJson.spaces2)
    println(member)
    member
  }
}
