package controllers.admin.system

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.controllers.AuthController
import com.kyleu.projectile.models.Application
import com.kyleu.projectile.models.auth.UserCredentials
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.Call
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

@javax.inject.Singleton
class SearchController @javax.inject.Inject() (
    override val app: Application, injector: Injector
)(implicit ec: ExecutionContext) extends AuthController("search") {
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
      case r => Ok(com.kyleu.projectile.views.html.admin.explore.searchResults(q, r.map(_._2), app.cfg(Some(request.identity), admin = true, "Search", q)))
    }
  }

  private[this] def searchInt(creds: UserCredentials, q: String, id: Int)(implicit timing: TraceData) = {
    val intSearches = Seq.empty[Future[Seq[(Call, Html)]]] ++
      /* Start int searches */
      /* End int searches */
      Nil

    Future.sequence(intSearches).map(_.flatten)
  }

  private[this] def searchUuid(creds: UserCredentials, q: String, id: UUID)(implicit timing: TraceData) = {
    val uuidSearches = Seq.empty[Future[Seq[(Call, Html)]]] ++
      /* Start uuid searches */
        /* End uuid searches */
        Nil

    Future.sequence(uuidSearches).map(_.flatten)
  }

  private[this] def searchString(creds: UserCredentials, q: String)(implicit timing: TraceData) = {
    val stringSearches = Seq.empty[Future[Seq[(Call, Html)]]] ++
      /* Start string searches */
        /* End string searches */
        Nil

    Future.sequence(stringSearches).map(_.flatten)
  }
}
