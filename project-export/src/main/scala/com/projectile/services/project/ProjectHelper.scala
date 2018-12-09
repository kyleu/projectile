package com.projectile.services.project

import io.scalaland.chimney.dsl._
import com.projectile.models.command.ProjectileCommand._
import com.projectile.models.command.ProjectileResponse._
import com.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.projectile.models.export.config.ExportConfiguration
import com.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}
import com.projectile.models.project.{Project, ProjectSummary}
import com.projectile.services.ProjectileService
import com.projectile.services.output.OutputService
import com.projectile.services.project.audit.ProjectAuditService
import com.projectile.services.project.update.ProjectUpdateService
import com.projectile.util.JsonSerializers._

trait ProjectHelper { this: ProjectileService =>
  private[this] lazy val summarySvc = new ProjectSummaryService(cfg)

  private[this] lazy val enumSvc = new EnumMemberService(this)
  private[this] lazy val modelSvc = new ModelMemberService(this)
  private[this] lazy val serviceSvc = new ServiceMemberService(this)

  private[this] lazy val exportSvc = new ProjectExportService(this)
  private[this] lazy val outputSvc = new OutputService(cfg.workingDirectory)

  private[this] val dir = cfg.projectDirectory

  def listProjects() = summarySvc.list()

  def getProject(key: String) = load(key)
  def getProjectSummary(key: String) = summarySvc.getSummary(key)
  def updateProject(key: String) = ProjectUpdateService.update(this, load(key))
  def saveProject(summary: ProjectSummary) = summarySvc.add(summary)
  def removeProject(key: String) = removeProjectFiles(key)

  def saveEnumMembers(key: String, members: Seq[EnumMember]) = enumSvc.saveEnums(key, members)
  def saveEnumMember(key: String, member: EnumMember) = saveEnumMembers(key, Seq(member)).head
  def removeEnumMember(key: String, member: String) = enumSvc.removeEnum(key, member)

  def saveModelMembers(key: String, members: Seq[ModelMember]) = modelSvc.saveModels(key, members)
  def saveModelMember(key: String, member: ModelMember) = saveModelMembers(key, Seq(member)).head
  def removeModelMember(key: String, member: String) = modelSvc.removeModel(key, member)

  def saveServiceMembers(key: String, members: Seq[ServiceMember]) = serviceSvc.saveServices(key, members)
  def saveServiceMember(key: String, member: ServiceMember) = saveServiceMembers(key, Seq(member)).head
  def removeServiceMember(key: String, member: String) = serviceSvc.removeService(key, member)

  def exportProject(key: String, verbose: Boolean) = {
    val o = exportSvc.exportProject(projectRoot = cfg.workingDirectory, key = key, verbose = verbose)
    o -> outputSvc.persist(o = o, verbose = verbose)
  }
  def auditProject(key: String, verbose: Boolean) = {
    val c = loadConfig(key)
    val o = exportSvc.getOutput(projectRoot = cfg.workingDirectory, key = key, verbose = verbose)
    ProjectAuditService.audit(cfg.workingDirectory, c, o)
  }

  def loadConfig(key: String) = {
    val p = getProject(key)
    val inputs = (p.enums.map(_.input) ++ p.models.map(_.input)).distinct.map(getInput).map(i => i.key -> i).toMap

    val exportEnums = p.enums.map(e => inputs(e.input).exportEnum(e.key).apply(e))
    val exportModels = p.models.map(e => inputs(e.input).exportModel(e.key).apply(e))
    val exportServices = p.services.map(e => inputs(e.input).exportService(e.key).apply(e))

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels, services = exportServices)
  }

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(listProjects())
    case GetProject(key) => ProjectDetail(getProject(key))
    case UpdateProject(key) => ProjectUpdateResult(updateProject(key))
    case AddProject(p) => ProjectDetail(saveProject(p))
    case RemoveProject(key) => removeProject(key)

    case SaveEnumMembers(p, members) => JsonResponse(saveEnumMembers(p, members).asJson)
    case RemoveEnumMember(p, member) => JsonResponse(removeEnumMember(p, member).asJson)

    case SaveModelMembers(p, members) => JsonResponse(saveModelMembers(p, members).asJson)
    case RemoveModelMember(p, member) => JsonResponse(removeModelMember(p, member).asJson)

    case SaveServiceMembers(p, members) => JsonResponse(saveServiceMembers(p, members).asJson)
    case RemoveServiceMember(p, member) => JsonResponse(removeServiceMember(p, member).asJson)

    case ExportProject(key) =>
      val r = exportProject(key, verbose = false)
      ProjectExportResult(r._1, r._2)
    case AuditProject(key) => JsonResponse(auditProject(key, verbose = false).asJson)
  }

  private[this] def removeProjectFiles(key: String) = {
    (dir / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  private[this] def load(key: String) = summarySvc.getSummary(key)
    .getOrElse(throw new IllegalStateException(s"No project found with key [$key]"))
    .into[Project]
    .withFieldComputed(_.enums, _ => loadDir[EnumMember](s"$key/enum"))
    .withFieldComputed(_.models, _ => loadDir[ModelMember](s"$key/model"))
    .withFieldComputed(_.services, _ => loadDir[ServiceMember](s"$key/service"))
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
