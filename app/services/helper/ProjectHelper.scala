package services.helper

import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.command.{ProjectileCommand, ProjectileResponse}
import models.project.ProjectSummary
import models.project.member.ProjectMember
import services.ProjectileService
import services.project._
import util.JsonSerializers._

trait ProjectHelper { this: ProjectileService =>
  private[this] val projectSummarySvc = new ProjectSummaryService(cfg)
  private[this] lazy val projectMemberSvc = new ProjectMemberService(cfg)
  private[this] lazy val projectActionSvc = new ProjectActionService(cfg)

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(projectSummarySvc.list())
    case GetProject(key) => ProjectDetail(projectSummarySvc.load(key))
    case AddProject(p) => ProjectDetail(projectSummarySvc.add(p))
    case RemoveProject(key) => projectSummarySvc.remove(key)

    case RemoveProjectMember(p, t, member) => JsonResponse(projectMemberSvc.remove(p, t, member).asJson)
    case SaveProjectMember(p, member) => JsonResponse(projectMemberSvc.save(p, member).asJson)

    case ExportProject(key) => JsonResponse(projectActionSvc.export(key).asJson)
    case AuditProject(key) => JsonResponse(projectActionSvc.audit(key).asJson)
  }

  def listProjects() = process(ListProjects).asInstanceOf[ProjectList].projects

  def getProject(key: String) = process(GetProject(key)).asInstanceOf[ProjectDetail].project
  def addProject(summary: ProjectSummary) = process(AddProject(summary)).asInstanceOf[ProjectDetail].project
  def removeProject(key: String) = process(RemoveProject(key))

  def saveProjectMember(key: String, member: ProjectMember) = {
    process(SaveProjectMember(key, member)).asInstanceOf[JsonResponse].json
  }
  def removeProjectMember(key: String, t: ProjectMember.OutputType, member: String) = {
    process(RemoveProjectMember(key, t, member)).asInstanceOf[JsonResponse].json
  }

  def exportProject(key: String) = process(ExportProject(key)).asInstanceOf[JsonResponse].json
  def auditProject(key: String) = process(AuditProject(key)).asInstanceOf[JsonResponse].json
}
