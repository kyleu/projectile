// scalastyle:off file.size.limit
package com.kyleu.projectile.controllers.admin.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.models.module.{Application, ApplicationFeature}
import com.kyleu.projectile.models.web.InternalIcons
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.services.search.SearchProvider
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@javax.inject.Singleton
class SearchController @javax.inject.Inject() (
    override val app: Application, injector: Injector, provider: SearchProvider
)(implicit ec: ExecutionContext) extends AuthController("search") {
  ApplicationFeature.enable(ApplicationFeature.Search)
  PermissionService.registerModel("tools", "Search", "System Search", Some(InternalIcons.search), "search")

  def search(q: String) = withSession("search", ("tools", "Search", "search")) { implicit request => implicit td =>
    val t = q.trim
    val creds = UserCredentials.fromRequest(request)
    val results = try {
      searchInt(creds, t, t.toInt)
    } catch {
      case _: NumberFormatException => try {
        searchUuid(creds, t, UUID.fromString(t))
      } catch {
        case _: IllegalArgumentException => searchString(creds, t)
      }
    }
    results.map {
      case r if r.size == 1 => Redirect(r.headOption.getOrElse(throw new IllegalStateException())._1)
      case r =>
        val cfg = app.cfg(u = Some(request.identity), "Search", q)
        Ok(com.kyleu.projectile.views.html.admin.explore.searchResults(q, r.map(_._2), cfg))
    }
  }

  private[this] def searchInt(creds: UserCredentials, q: String, id: Int)(implicit timing: TraceData) = {
    val intSearches = provider.intSearches(app, injector, creds)(q, id)(ec, timing).map(_.recover { case NonFatal(x) => Seq.empty })
    Future.sequence(intSearches).map(_.flatten)
  }

  private[this] def searchUuid(creds: UserCredentials, q: String, id: UUID)(implicit timing: TraceData) = {
    val uuidSearches = provider.uuidSearches(app, injector, creds)(q, id)(ec, timing) ++ (
      InternalUuidSearchHelpers.uuid(q, id, injector, creds)
    ).flatten.map(_.recover { case NonFatal(x) => Seq.empty })

    Future.sequence(uuidSearches).map(_.flatten)
  }

  private[this] def searchString(creds: UserCredentials, q: String)(implicit timing: TraceData) = {
    val stringSearches = provider.stringSearches(app, injector, creds)(q)(ec, timing) ++ (
      InternalStringSearchHelpersNew.string(q, injector, creds)
    ).flatten.map(_.recover { case NonFatal(x) => Seq.empty })
    Future.sequence(stringSearches).map(_.flatten)
  }
}
