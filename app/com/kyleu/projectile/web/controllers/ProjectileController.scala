package com.kyleu.projectile.web.controllers

import play.api.mvc.InjectedController
import com.kyleu.projectile.web.util.PlayServerHelper

class ProjectileController extends InjectedController {
  protected[this] def projectile = PlayServerHelper.svc
}
