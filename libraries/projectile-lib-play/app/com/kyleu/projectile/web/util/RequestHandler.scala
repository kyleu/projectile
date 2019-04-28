package com.kyleu.projectile.web.util

import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import javax.inject.Inject
import play.api.OptionalDevContext
import play.api.http._
import play.api.mvc.RequestHeader
import play.api.routing.Router
import play.core.{DefaultWebCommands, WebCommands}

class RequestHandler @Inject() (
    webCommands: WebCommands,
    optionalDevContext: OptionalDevContext,
    errorHandler: HttpErrorHandler,
    configuration: HttpConfiguration,
    router: Router,
    filters: HttpFilters
) extends DefaultHttpRequestHandler(new DefaultWebCommands, None, router, errorHandler, configuration, filters.filters) with Logging {

  override def routeRequest(request: RequestHeader) = {
    if (!Option(request.path).exists(x => x.startsWith("/assets") || x.startsWith("/style") || x.startsWith("/components"))) {
      log.info(s"Request from [${request.remoteAddress}]: ${request.toString()}")(TraceData.noop)
    }
    super.routeRequest(request)
  }
}
