package services.helper

import io.circe.Json
import models.command.ProjectileCommand._
import models.command.ProjectileResponse._
import models.command.{ProjectileCommand, ProjectileResponse}
import models.project.{ProjectEnum, ProjectModel, ProjectSummary, ProjectSvc}
import services.ProjectileService
import services.project.{ProjectActionService, ProjectSummaryService}
import util.JsonSerializers._

trait ProjectHelper { this: ProjectileService =>
  private[this] val projectSummarySvc = new ProjectSummaryService(cfg)
  private[this] val projectActionSvc = new ProjectActionService(cfg)

  protected val processProject: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case ListProjects => ProjectList(projectSummarySvc.list())
    case GetProject(key) => ProjectDetail(projectSummarySvc.load(key))
    case AddProject(p) => ProjectDetail(projectSummarySvc.add(p))
    case RemoveProject(key) => projectSummarySvc.remove(key)
    case ExportProject(key) => JsonResponse(projectActionSvc.export(key).asJson)
    case AuditProject(key) => JsonResponse(projectActionSvc.audit(key).asJson)

    case RemoveProjectEnum(p, enum) => JsonResponse(Json.fromString(s"TODO $p: $enum"))
    case SaveProjectEnum(p, enum) => JsonResponse(Json.fromString(s"TODO $p: $enum"))

    case RemoveProjectModel(p, model) => JsonResponse(Json.fromString(s"TODO $p: $model"))
    case SaveProjectModel(p, model) => JsonResponse(Json.fromString(s"TODO $p: $model"))

    case RemoveProjectService(p, svc) => JsonResponse(Json.fromString(s"TODO $p: $svc"))
    case SaveProjectService(p, svc) => JsonResponse(Json.fromString(s"TODO $p: $svc"))
  }

  def listProjects() = process(ListProjects).asInstanceOf[ProjectList].projects

  def getProject(key: String) = process(GetProject(key)).asInstanceOf[ProjectDetail].project
  def addProject(summary: ProjectSummary) = process(AddProject(summary)).asInstanceOf[ProjectDetail].project
  def removeProject(key: String) = process(RemoveProject(key))
  def exportProject(key: String) = process(ExportProject(key)).asInstanceOf[JsonResponse].json
  def auditProject(key: String) = process(AuditProject(key)).asInstanceOf[JsonResponse].json

  def saveProjectEnum(key: String, enum: ProjectEnum) = process(SaveProjectEnum(key, enum)).asInstanceOf[JsonResponse].json
  def removeProjectEnum(key: String, enum: String) = process(RemoveProjectEnum(key, enum)).asInstanceOf[JsonResponse].json

  def saveProjectModel(key: String, model: ProjectModel) = process(SaveProjectModel(key, model)).asInstanceOf[JsonResponse].json
  def removeProjectModel(key: String, model: String) = process(RemoveProjectModel(key, model)).asInstanceOf[JsonResponse].json

  def saveProjectService(key: String, svc: ProjectSvc) = process(SaveProjectService(key, svc)).asInstanceOf[JsonResponse].json
  def removeProjectService(key: String, svc: String) = process(RemoveProjectService(key, svc)).asInstanceOf[JsonResponse].json
}
