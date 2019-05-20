package com.kyleu.projectile.controllers

import play.api.http.HttpErrorHandler

object Assets {
  def path(p: String) = com.kyleu.projectile.controllers.routes.Assets.versioned(p).url
}

class Assets @javax.inject.Inject() (eh: HttpErrorHandler, md: controllers.AssetsMetadata) extends controllers.AssetsBuilder(eh, md)
