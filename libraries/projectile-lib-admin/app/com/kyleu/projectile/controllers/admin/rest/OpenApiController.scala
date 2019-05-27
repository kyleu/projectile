package com.kyleu.projectile.controllers.admin.rest

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.controllers.admin.rest.routes.OpenApiController
import com.kyleu.projectile.models.menu.SystemMenu
import com.kyleu.projectile.models.module.ApplicationFeature.Rest.value
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.util.{JsonIncludeParser, JsonSerializers}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.control.NonFatal

@javax.inject.Singleton
class OpenApiController @javax.inject.Inject() (override val app: Application)(implicit ec: ExecutionContext) extends AuthController("rest") {
  ApplicationFeature.enable(ApplicationFeature.Rest)
  PermissionService.registerModel("tools", "Rest", "Swagger UI", Some(InternalIcons.rest), "ide")
  SystemMenu.addRootMenu(value, "Swagger UI", Some("OpenAPI IDE for exploring the system"), OpenApiController.ui(), InternalIcons.rest)

  private[this] def loadJson(key: String) = {
    val resource = Option(getClass.getClassLoader.getResourceAsStream(key)).getOrElse(throw new IllegalStateException(s"Cannot load [$key] from classpath."))
    val content = Source.fromInputStream(resource).getLines.filterNot(_.trim.startsWith("//")).mkString("\n")
    JsonSerializers.parseJson(content) match {
      case Left(x) => throw new IllegalStateException(s"Cannot parse json from [$key].", x)
      case Right(json) => json
    }
  }

  val rootFilename = "openapi/openapi.json"

  lazy val jsonContent = try {
    val json = new JsonIncludeParser(loadJson).parseWithIncludes(rootFilename)
    Future.successful(Ok(json))
  } catch {
    case NonFatal(x) =>
      log.error(s"Unable to parse json includes from [$rootFilename].", x)(TraceData.noop)
      Future.successful(InternalServerError(x.getMessage))
  }

  def json() = withSession("openapi.json", ("tools", "Rest", "ide")) { _ => _ =>
    jsonContent
  }

  def ui() = withSession("index", ("tools", "Rest", "ide")) { implicit request => implicit td =>
    val cfg = app.cfg(u = Some(request.identity), "system", "rest")
    Future.successful(Ok(com.kyleu.projectile.views.html.rest.swagger(cfg)))
  }
}
