package com.kyleu.projectile.web.controllers.input

import com.kyleu.projectile.web.controllers.ProjectileController
import com.kyleu.projectile.models.database.input.PostgresInput
import com.kyleu.projectile.models.graphql.input.GraphQLInput
import com.kyleu.projectile.models.input.{InputSummary, InputTemplate}
import com.kyleu.projectile.models.thrift.input.ThriftInput
import com.kyleu.projectile.web.util.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class InputController @javax.inject.Inject() () extends ProjectileController {
  def detail(key: String) = Action.async { implicit request =>
    val view = projectile.getInput(key) match {
      case i: PostgresInput => com.kyleu.projectile.web.views.html.input.postgresInput(projectile, i)
      case i: ThriftInput => com.kyleu.projectile.web.views.html.input.thriftInput(projectile, i)
      case g: GraphQLInput => com.kyleu.projectile.web.views.html.input.graphQLInput(projectile, g)
      case x => throw new IllegalStateException(s"Cannot render view for [$x]")
    }
    Future.successful(Ok(view))
  }

  def refresh(key: String) = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    projectile.refreshInput(key)
    val msg = s"Refreshed input [$key] in [${System.currentTimeMillis - startMs}ms]"
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.input.routes.InputController.detail(key)).flashing("success" -> msg))
  }

  def refreshAll = Action.async { implicit request =>
    val startMs = System.currentTimeMillis
    val results = projectile.listInputs().map(i => projectile.refreshInput(i.key))
    val msg = s"Refreshed [${results.size}] inputs in [${System.currentTimeMillis - startMs}ms]"
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()).flashing("success" -> msg))
  }

  def formNew = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.input.formNew(projectile)))
  }

  def form(key: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.input.form(projectile, projectile.getInput(key))))
  }

  def save() = Action.async { implicit request =>
    val form = ControllerUtils.getForm(request.body)
    val summary = InputSummary(
      template = InputTemplate.withValue(form("template")),
      key = form("key"),
      description = form("description")
    )
    val input = projectile.addInput(summary)
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.input.routes.InputController.detail(input.key)).flashing("success" -> s"Saved input [${input.key}]"))
  }

  def remove(key: String) = Action.async { implicit request =>
    val removed = projectile.removeInput(key)
    Future.successful(Redirect(com.kyleu.projectile.web.controllers.routes.HomeController.index()).flashing("success" -> s"Removed input [$key]: $removed"))
  }

  def enumDetail(key: String, enum: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.input.detailEnum(projectile, projectile.getInput(key).getEnum(enum))))
  }

  def modelDetail(key: String, model: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.input.detailModel(projectile, projectile.getInput(key).getModel(model))))
  }

  def serviceDetail(key: String, service: String) = Action.async { implicit request =>
    Future.successful(Ok(com.kyleu.projectile.web.views.html.input.detailService(projectile, projectile.getInput(key).getService(service))))
  }
}
