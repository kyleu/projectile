package com.kyleu.projectile.services.search

import java.util.UUID

import com.google.inject.Injector
import com.kyleu.projectile.models.module.Application
import com.kyleu.projectile.services.auth.PermissionService
import com.kyleu.projectile.util.Credentials
import com.kyleu.projectile.util.tracing.TraceData
import play.api.mvc.Call
import play.twirl.api.Html

import scala.concurrent.{ExecutionContext, Future}

@com.github.ghik.silencer.silent
class SearchProvider {
  protected[this] def act[T: reflect.ClassTag, U](
    injector: Injector, creds: Credentials, perm: (String, String, String), f: T => Future[Seq[U]], v: U => Call, s: U => Html
  )(implicit ec: ExecutionContext, td: TraceData) = PermissionService.check(creds.role, perm._1, perm._2, perm._3) match {
    case (false, _) => Future.successful(Nil)
    case (true, _) =>
      val svc = injector.getInstance(implicitly[reflect.ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])
      f(svc).map(_.map(model => v(model) -> s(model)).toSeq)
  }

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
