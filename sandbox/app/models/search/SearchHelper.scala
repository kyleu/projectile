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
    /* Projectile export section [sandbox] */
    Seq(
      act[services.size.BigRowService, models.size.BigRow](injector = injector, creds = creds, perm = ("size", "BigRow", "view"), f = _.getById(creds, id.toLong), v = model => controllers.admin.size.routes.BigRowController.view(model.id), s = model => views.html.admin.size.bigRowSearchResult(model, s"Big [${model.id}] matched id [$q]")),
      act[services.size.SmallRowService, models.size.SmallRow](injector = injector, creds = creds, perm = ("size", "SmallRow", "view"), f = _.getById(creds, id.toLong), v = model => controllers.admin.size.routes.SmallRowController.view(model.id), s = model => views.html.admin.size.smallRowSearchResult(model, s"Small [${model.id}] matched id [$q]"))
    ) ++
      /* End int searches */
      Nil
  }

  override def uuidSearches(app: Application, injector: Injector, creds: Credentials)(q: String, id: UUID)(implicit ec: ExecutionContext, td: TraceData) = {
    /* Start uuid searches */
    /* Projectile export section [sandbox] */
    Seq(
      act[services.b.BottomRowService, models.b.BottomRow](injector = injector, creds = creds, perm = ("b", "BottomRow", "view"), f = _.getById(creds, id), v = model => controllers.admin.b.routes.BottomRowController.view(model.id), s = model => views.html.admin.b.bottomRowSearchResult(model, s"Bottom [${model.id}] matched id [$q]")),
      act[services.t.TopRowService, models.t.TopRow](injector = injector, creds = creds, perm = ("t", "TopRow", "view"), f = _.getById(creds, id), v = model => controllers.admin.t.routes.TopRowController.view(model.id), s = model => views.html.admin.t.topRowSearchResult(model, s"Top [${model.id}] matched id [$q]"))
    ) ++
      /* End uuid searches */
      Nil
  }

  override def stringSearches(app: Application, injector: Injector, creds: Credentials)(q: String)(implicit ec: ExecutionContext, td: TraceData) = {
    /* Start string searches */
    /* Projectile export section [sandbox] */
    Seq(
      act[services.size.BigRowService, models.size.BigRow](injector = injector, creds = creds, perm = ("size", "BigRow", "view"), f = _.searchExact(creds, q = q, limit = Some(5)), v = model => controllers.admin.size.routes.BigRowController.view(model.id), s = model => views.html.admin.size.bigRowSearchResult(model, s"Big [${model.id}] matched t [$q]")),
      act[services.b.BottomRowService, models.b.BottomRow](injector = injector, creds = creds, perm = ("b", "BottomRow", "view"), f = _.searchExact(creds, q = q, limit = Some(5)), v = model => controllers.admin.b.routes.BottomRowController.view(model.id), s = model => views.html.admin.b.bottomRowSearchResult(model, s"Bottom [${model.id}] matched t [$q]")),
      act[services.size.SmallRowService, models.size.SmallRow](injector = injector, creds = creds, perm = ("size", "SmallRow", "view"), f = _.searchExact(creds, q = q, limit = Some(5)), v = model => controllers.admin.size.routes.SmallRowController.view(model.id), s = model => views.html.admin.size.smallRowSearchResult(model, s"Small [${model.id}] matched t [$q]")),
      act[services.t.TopRowService, models.t.TopRow](injector = injector, creds = creds, perm = ("t", "TopRow", "view"), f = _.searchExact(creds, q = q, limit = Some(5)), v = model => controllers.admin.t.routes.TopRowController.view(model.id), s = model => views.html.admin.t.topRowSearchResult(model, s"Top [${model.id}] matched t [$q]"))
    ) ++
      /* End string searches */
      Nil
  }
}
