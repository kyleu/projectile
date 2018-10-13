package services.project

import models.project.member.ProjectMember
import services.config.ConfigService
import util.JsonSerializers._

class ProjectMemberService(val cfg: ConfigService) {
  private[this] def fileFor(p: String, t: ProjectMember.OutputType, k: String) = cfg.projectDir(p) / t.value / (k + ".json")

  def save(p: String, member: ProjectMember) = {
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
