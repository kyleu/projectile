package com.kyleu.projectile.services.project

import better.files.File
import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.models.command.ProjectileResponse._
import com.kyleu.projectile.models.export.config.ExportConfiguration
import com.kyleu.projectile.models.project.member.{EnumMember, ModelMember, ServiceMember}
import com.kyleu.projectile.models.project.{Project, ProjectSummary}
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.output.OutputService
import com.kyleu.projectile.util.JsonFileLoader
import com.kyleu.projectile.util.JsonSerializers._
import io.scalaland.chimney.dsl._

trait ProjectHelper { this: ProjectileService =>
  private[this] lazy val summarySvc = new ProjectSummaryService(rootCfg)

  private[this] lazy val enumSvc = new EnumMemberService(this)
  private[this] lazy val modelSvc = new ModelMemberService(this)
  private[this] lazy val serviceSvc = new ServiceMemberService(this)

  private[this] lazy val exportSvc = new ProjectExportService(this)
  private[this] lazy val outputSvc = new OutputService(this)

  def listProjects() = summarySvc.list()

  def getProject(key: String) = load(configForProject(key).projectDirectory, key)
  def getProjectSummaryOpt(key: String) = summarySvc.getSummary(key)
  def getProjectSummary(key: String) = getProjectSummaryOpt(key).getOrElse(throw new IllegalStateException(s"No project with key [$key]"))

  def updateProject(key: String) = ProjectUpdateService.update(this, load(configForProject(key).projectDirectory, key))
  def updateAll() = listProjects().flatMap(project => updateProject(key = project.key))

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
    val o = exportSvc.getOutput(projectRoot = configForProject(key).workingDirectory, key = key, verbose = verbose)
    o -> outputSvc.persist(o = o, verbose = verbose)
  }
  def exportAll() = listProjects().map(project => exportProject(key = project.key, verbose = false))

  def loadConfig(key: String) = {
    val p = getProject(key)
    val input = getInput(p.input)

    val exportEnums = p.enums.map(e => input.exportEnum(e.key).apply(e))
    val exportModels = p.models.map(e => input.exportModel(e.key).apply(e))
    val exportServices = p.services.map(e => input.exportService(e.key).apply(e))

    ExportConfiguration(project = p, enums = exportEnums, models = exportModels, services = exportServices)
  }

  private[this] def removeProjectFiles(key: String) = {
    (configForProject(key).projectDirectory / key).delete(swallowIOExceptions = true)
    ProjectileResponse.OK
  }

  private[this] def load(dir: File, key: String) = summarySvc.getSummary(key)
    .getOrElse(throw new IllegalStateException(s"No project found with key [$key]"))
    .into[Project]
    .withFieldComputed(_.enums, _ => loadDir[EnumMember](dir, s"$key/enum"))
    .withFieldComputed(_.models, _ => loadDir[ModelMember](dir, s"$key/model"))
    .withFieldComputed(_.services, _ => loadDir[ServiceMember](dir, s"$key/service"))
    .transform

  private[this] def loadDir[A: Decoder](dir: File, k: String) = {
    val d = dir / k
    if (d.exists && d.isDirectory && d.isReadable) {
      d.children.filter(f => f.isRegularFile && f.name.endsWith(".json")).map(f => JsonFileLoader.loadFile[A](f, k)).toList
    } else {
      Nil
    }
  }

  protected[this] def projectResults(k: String) = {
    val r = exportProject(k, verbose = false)
    ProjectExportResult(r._1, r._2)
  }
}
