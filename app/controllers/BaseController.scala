package controllers

import play.api.mvc.InjectedController
import util.web.PlayServerHelper

class BaseController extends InjectedController {
  protected[this] def projectile = PlayServerHelper.svc
}
