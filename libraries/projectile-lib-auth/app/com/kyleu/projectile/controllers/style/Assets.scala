package com.kyleu.projectile.controllers.style

import javax.inject._
import play.api.http.HttpErrorHandler

class Assets @Inject() (
    errorHandler: HttpErrorHandler,
    assetsMetadata: controllers.AssetsMetadata
) extends controllers.AssetsBuilder(errorHandler, assetsMetadata)
