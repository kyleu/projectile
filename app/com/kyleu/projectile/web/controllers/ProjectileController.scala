package com.kyleu.projectile.web.controllers

import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.InjectedController
import com.kyleu.projectile.web.util.PlayServerHelper

class ProjectileController extends InjectedController {
  protected[this] implicit val td: TraceData = TraceData.noop

  protected[this] def projectile = PlayServerHelper.svc
}
