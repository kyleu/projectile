package services.project

import models.project.member.ProjectMember
import services.ProjectileService
import util.JsonSerializers._

class ProjectMemberService(val svc: ProjectileService) {
  private[this] def fileFor(p: String, t: ProjectMember.OutputType, k: String) = svc.cfg.projectDir(p) / t.value / (k + ".json")

  def save(p: String, member: ProjectMember) = {
    val i = svc.getInput(member.input)
    val m = member.inputType.out match {
      case ProjectMember.OutputType.Model => i.exportModel(member.inputKey)
      case ProjectMember.OutputType.Enum => i.exportEnum(member.inputKey)
    }

    val f = fileFor(p, member.outputType, member.outputKey)
    f.createFileIfNotExists(createParents = true)
    f.overwrite(member.asJson.spaces2)
    member
  }

  def remove(p: String, t: ProjectMember.OutputType, member: String) = {
    val f = fileFor(p, t, member)
    f.delete(true)
    s"Removed $t [$member]"
  }
}
