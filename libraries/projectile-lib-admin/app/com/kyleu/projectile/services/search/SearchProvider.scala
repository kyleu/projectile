package com.kyleu.projectile.services.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.Credentials
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.Call
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

@com.github.ghik.silencer.silent
class SearchProvider {
  def intSearches(app: Application, injector: Injector, creds: Credentials)(
    q: String, id: Int
  )(implicit ec: ExecutionContext, td: TraceData): Seq[Future[Seq[(Call, Html)]]] = Nil

  def uuidSearches(app: Application, injector: Injector, creds: Credentials)(
    q: String, id: UUID
  )(implicit ec: ExecutionContext, td: TraceData): Seq[Future[Seq[(Call, Html)]]] = Nil

  def stringSearches(app: Application, injector: Injector, creds: Credentials)(
    q: String
  )(implicit ec: ExecutionContext, td: TraceData): Seq[Future[Seq[(Call, Html)]]] = Nil
}
