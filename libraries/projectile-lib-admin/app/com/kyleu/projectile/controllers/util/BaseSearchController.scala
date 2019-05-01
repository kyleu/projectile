package com.kyleu.projectile.controllers.util

import java.util.UUID

import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.util.tracing.TraceData
import com.kyleu.projectile.views.html.admin.explore.searchResults
import play.api.mvc.Call
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseSearchController(implicit ec: ExecutionContext) extends AuthController("search") {
  def search(q: String) = withSession("admin.search", admin = true) { implicit request => implicit td =>
    val creds = UserCredentials.fromRequest(request)
    val results = try {
      searchInt(creds, q, q.toInt)
    } catch {
      case _: NumberFormatException => try {
        searchUuid(creds, q, UUID.fromString(q))
      } catch {
        case _: IllegalArgumentException => searchString(creds, q)
      }
    }
    results.map {
      case r if r.size == 1 => Redirect(r.head._1)
      case r => Ok(searchResults(q, r.map(_._2), app.cfg(Some(request.identity), admin = true, "Search", q)))
    }
  }

  protected def searchInt(creds: UserCredentials, q: String, id: Int)(implicit timing: TraceData): Future[Seq[(Call, Html)]]

  protected def searchUuid(creds: UserCredentials, q: String, id: UUID)(implicit timing: TraceData): Future[Seq[(Call, Html)]]

  protected def searchString(creds: UserCredentials, q: String)(implicit timing: TraceData): Future[Seq[(Call, Html)]]
}
