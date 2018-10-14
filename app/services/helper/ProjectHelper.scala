package services.helper

import io.scalaland.chimney.dsl._
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.command.{ProjectileCommand, ProjectileResponse}
import models.project.{Project, ProjectSummary}
import models.project.member.ProjectMember
import services.ProjectileService
import services.project._
import util.JsonSerializers._

trait ProjectHelper { this: ProjectileService =>
  private[this] lazy val summarySvc = new ProjectSummaryService(cfg)
  private[this] lazy val memberSvc = new ProjectMemberService(cfg)
  private[this] lazy val exportSvc = new ProjectExportService(this)
  private[this] lazy val auditSvc = new ProjectAuditService()

  private[this] val dir = cfg.projectDirectory

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(summarySvc.list())
    case GetProject(key) => ProjectDetail(load(key))
    case AddProject(p) => ProjectDetail(summarySvc.add(p))
    case RemoveProject(key) => removeProjectFiles(key)

    case RemoveProjectMember(p, t, member) => JsonResponse(memberSvc.remove(p, t, member).asJson)
    case SaveProjectMember(p, member) => JsonResponse(memberSvc.save(p, member).asJson)

    case ExportProject(key) => ProjectExportResult(exportSvc.exportProject(key = key, verbose = false))
    case AuditProject(key) => JsonResponse(auditSvc.audit(key).asJson)
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

  def exportProject(key: String) = process(ExportProject(key)).asInstanceOf[ProjectExportResult]
  def auditProject(key: String) = process(AuditProject(key)).asInstanceOf[ProjectAuditResult]

  private[this] def removeProjectFiles(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  private[this] def load(key: String) = summarySvc.getSummary(key).into[Project]
    .withFieldComputed(_.enums, _ => loadDir[ProjectMember](s"$key/enum"))
    .withFieldComputed(_.models, _ => loadDir[ProjectMember](s"$key/model"))
    .transform

  private[this] def loadDir[A: Decoder](k: String) = {
    val d = dir / k
    if (d.exists && d.isDirectory && d.isReadable) {
      d.children.map(f => loadFile[A](f, k)).toList
    } else {
      Nil
    }
  }
}
