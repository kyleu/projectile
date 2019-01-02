package com.kyleu.projectile.web.controllers

import play.api.mvc.InjectedController
import com.kyleu.projectile.web.util.PlayServerHelper

class BaseController extends InjectedController {
  protected[this] def projectile = PlayServerHelper.svc
}
