package com.kyleu.projectile.components.controllers

import play.api.mvc.InjectedController

import scala.concurrent.Future

@javax.inject.Singleton
class ComponentController @javax.inject.Inject() () extends InjectedController {
  def test = Action.async { implicit request =>
    Future.successful(Ok("Hello!"))
  }
}
