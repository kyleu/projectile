package com.kyleu.projectile.services.project

import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.input.Input
import com.kyleu.projectile.models.output.OutputPath
import com.kyleu.projectile.models.output.file.OutputFile
import com.kyleu.projectile.models.project.ProjectSummary
import com.kyleu.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember, UnionMember}
import com.kyleu.projectile.models.typescript.input.TypeScriptInput
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService

trait ProjectHelper { this: ProjectileService =>
  private[this] lazy val summarySvc = new ProjectSummaryService(rootCfg)

  private[this] lazy val enumSvc = new EnumMemberService(this)
  private[this] lazy val modelSvc = new ModelMemberService(this)
  private[this] lazy val unionSvc = new UnionMemberService(this)
  private[this] lazy val serviceSvc = new ServiceMemberService(this)

  private[this] lazy val loadSvc = new ProjectLoadService(this)
  private[this] lazy val exportSvc = new ProjectExportService(this)
  private[this] lazy val outputSvc = new OutputService(this)

  def listProjects() = summarySvc.list()

  def getProject(key: String) = loadSvc.load(configForProject(key), key)
  def getProjectSummaryOpt(key: String) = summarySvc.getSummary(key)
  def getProjectSummary(key: String) = getProjectSummaryOpt(key).getOrElse(throw new IllegalStateException(s"No project with key [$key]"))

  def updateProject(key: String) = ProjectUpdateService.update(this, loadSvc.load(configForProject(key), key))
  def updateAll() = listProjects().flatMap(project => updateProject(key = project.key))

  def saveProject(summary: ProjectSummary) = summarySvc.add(summary)
  def removeProject(key: String) = removeProjectFiles(key)

  def saveEnumMembers(key: String, members: Seq[EnumMember]) = enumSvc.saveEnums(key, members)
  def saveEnumMember(key: String, member: EnumMember) = saveEnumMembers(key, Seq(member)).headOption.getOrElse(throw new IllegalStateException())
  def removeEnumMember(key: String, member: String) = enumSvc.removeEnum(key, member)

  def saveModelMembers(key: String, members: Seq[ModelMember]) = modelSvc.saveModels(key, members)
  def saveModelMember(key: String, member: ModelMember) = saveModelMembers(key, Seq(member)).headOption.getOrElse(throw new IllegalStateException())
  def removeModelMember(key: String, member: String) = modelSvc.removeModel(key, member)

  def saveUnionMembers(key: String, members: Seq[UnionMember]) = unionSvc.saveUnions(key, members)
  def saveUnionMember(key: String, member: UnionMember) = saveUnionMembers(key, Seq(member)).headOption.getOrElse(throw new IllegalStateException())
  def removeUnionMember(key: String, member: String) = unionSvc.removeUnion(key, member)

  def saveServiceMembers(key: String, members: Seq[ServiceMember]) = serviceSvc.saveServices(key, members)
  def saveServiceMember(key: String, member: ServiceMember) = {
    saveServiceMembers(key, Seq(member)).headOption.getOrElse(throw new IllegalStateException())
  }
  def removeServiceMember(key: String, member: String) = serviceSvc.removeService(key, member)

  def exportProject(key: String, verbose: Boolean) = {
    val o = exportSvc.getOutput(projectRoot = configForProject(key).workingDirectory, key = key, verbose = verbose)
    o -> outputSvc.persist(o = o, verbose = verbose, cfg = configForProject(o.project.key))
  }
  def exportProjectFromInput(p: ProjectSummary, i: Input, cfg: ConfigService, verbose: Boolean) = {
    val projectRoot = cfg.workingDirectory
    val project = loadSvc.transform(projectRoot, p, i)
    val exportCfg = {
      ExportConfiguration(project = project, enums = i.enums, models = i.models, unions = i.unions, services = i.services, additional = i.additional)
    }
    val o = exportSvc.runExport(projectRoot = projectRoot, config = exportCfg, verbose = verbose)
    val res = outputSvc.persist(o, verbose = verbose, cfg = cfg)
    o -> res
  }
  def exportAll() = listProjects().map(project => exportProject(key = project.key, verbose = false))

  def loadExportConfig(key: String) = {
    val p = getProject(key)
    val input = p.getInput

    val exportEnums = p.enums.map(e => input.enum(e.key).apply(e))
    val exportModels = p.models.map(e => input.model(e.key).apply(e))
    val exportUnions = p.unions.map(u => input.union(u.key).apply(u))
    val exportServices = p.services.map(e => input.service(e.key).apply(e))
    val additional = input.additional

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels, unions = exportUnions, services = exportServices, additional = additional)
  }

  def getProjectStatus(key: String) = ProjectStatusService.status(getProject(key))

  private[this] def removeProjectFiles(key: String) = {
    (configForProject(key).projectDirectory / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK(s"Removed project [$key]")
  }

  protected[this] def projectResults(k: String) = {
    val r = exportProject(k, verbose = false)
    ProjectExportResult(r._1, r._2)
  }
}
