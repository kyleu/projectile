package services.helper

import io.scalaland.chimney.dsl._
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.command.{ProjectileCommand, ProjectileResponse}
import models.export.config.ExportConfiguration
import models.project.{Project, ProjectSummary}
import models.project.member.ProjectMember
import services.ProjectileService
import services.output.OutputService
import services.project._
import util.JsonSerializers._

trait ProjectHelper { this: ProjectileService =>
  private[this] lazy val summarySvc = new ProjectSummaryService(cfg)
  private[this] lazy val memberSvc = new ProjectMemberService(this)
  private[this] lazy val exportSvc = new ProjectExportService(this)
  private[this] lazy val outputSvc = new OutputService(cfg.workingDirectory)

  private[this] val dir = cfg.projectDirectory

  def listProjects() = summarySvc.list()

  def getProject(key: String) = load(key)
  def getProjectSummary(key: String) = summarySvc.getSummary(key)
  def saveProject(summary: ProjectSummary) = summarySvc.add(summary)
  def removeProject(key: String) = removeProjectFiles(key)

  def saveProjectMembers(key: String, members: Seq[ProjectMember]) = memberSvc.save(key, members)
  def removeProjectMember(key: String, t: ProjectMember.OutputType, member: String) = memberSvc.remove(key, t, member)

  def exportProject(key: String, verbose: Boolean) = {
    val o = exportSvc.exportProject(key = key, verbose = verbose)
    o -> outputSvc.persist(o = o, verbose = verbose)
  }
  def auditProject(key: String, verbose: Boolean) = {
    val c = loadConfig(key)
    c -> ProjectAuditService.audit(c)
  }

  def loadConfig(key: String) = {
    val p = getProject(key)
    val inputs = p.allMembers.map(_.input).distinct.map(getInput).map(i => i.key -> i).toMap

    // TODO apply overrides
    val exportEnums = p.enums.map(e => inputs(e.input).exportEnum(e.inputKey).apply(e))
    val exportModels = p.models.map(e => inputs(e.input).exportModel(e.inputKey).apply(e))

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels)
  }

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(listProjects())
    case GetProject(key) => ProjectDetail(getProject(key))
    case AddProject(p) => ProjectDetail(saveProject(p))
    case RemoveProject(key) => removeProject(key)

    case SaveProjectMembers(p, members) => JsonResponse(saveProjectMembers(p, members).asJson)
    case RemoveProjectMember(p, t, member) => JsonResponse(removeProjectMember(p, t, member).asJson)

    case ExportProject(key) =>
      val r = exportProject(key, verbose = false)
      ProjectExportResult(r._1, r._2.toMap)
    case AuditProject(key) => JsonResponse(auditProject(key, verbose = false).asJson)
  }

  private[this] def removeProjectFiles(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  private[this] def load(key: String) = summarySvc.getSummary(key)
    .getOrElse(throw new IllegalStateException(s"No project found with key [$key]"))
    .into[Project]
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
