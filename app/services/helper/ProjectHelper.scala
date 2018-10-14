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

  def listProjects() = summarySvc.list()

  def getProject(key: String) = load(key)
  def addProject(summary: ProjectSummary) = summarySvc.add(summary)
  def removeProject(key: String) = removeProjectFiles(key)

  def saveProjectMember(key: String, member: ProjectMember) = memberSvc.save(key, member)
  def removeProjectMember(key: String, t: ProjectMember.OutputType, member: String) = memberSvc.remove(key, t, member)

  def exportProject(key: String) = exportSvc.exportProject(key = key, verbose = false)
  def auditProject(key: String) = auditSvc.audit(key)

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(listProjects())
    case GetProject(key) => ProjectDetail(getProject(key))
    case AddProject(p) => ProjectDetail(addProject(p))
    case RemoveProject(key) => removeProject(key)

    case SaveProjectMember(p, member) => JsonResponse(saveProjectMember(p, member).asJson)
    case RemoveProjectMember(p, t, member) => JsonResponse(removeProjectMember(p, t, member).asJson)

    case ExportProject(key) => ProjectExportResult(exportProject(key))
    case AuditProject(key) => JsonResponse(auditProject(key).asJson)
  }

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
