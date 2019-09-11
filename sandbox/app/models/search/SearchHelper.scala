package models.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.search.SearchProvider
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData

import scala.concurrent.ExecutionContext

class SearchHelper extends SearchProvider {

  override def intSearches(app: Application, injector: Injector, creds: Credentials)(q: String, id: Int)(implicit ec: ExecutionContext, td: TraceData) = {
    /* Start int searches */
    /* End int searches */
    Nil
  }

  override def uuidSearches(app: Application, injector: Injector, creds: Credentials)(q: String, id: UUID)(implicit ec: ExecutionContext, td: TraceData) = {
    /* Start uuid searches */
    /* Projectile export section [sandbox] */
    Seq(
      act[services.BottomRowService, models.BottomRow](injector = injector, creds = creds, perm = ("system", "BottomRow", "view"), f = _.getByPrimaryKey(creds, id).map(_.toSeq), v = model => controllers.admin.system.routes.BottomRowController.view(model.id), s = model => views.html.admin.bottomRowSearchResult(model, s"Bottom [${model.id}] matched [$q]")),
      act[services.TopRowService, models.TopRow](injector = injector, creds = creds, perm = ("system", "TopRow", "view"), f = _.getByPrimaryKey(creds, id).map(_.toSeq), v = model => controllers.admin.system.routes.TopRowController.view(model.id), s = model => views.html.admin.topRowSearchResult(model, s"Top [${model.id}] matched [$q]"))
    ) ++
      /* End uuid searches */
      Nil
  }

  override def stringSearches(app: Application, injector: Injector, creds: Credentials)(q: String)(implicit ec: ExecutionContext, td: TraceData) = {
    /* Start string searches */
    /* End string searches */
    Nil
  }
}
