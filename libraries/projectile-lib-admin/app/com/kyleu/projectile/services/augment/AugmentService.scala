package com.kyleu.projectile.services.augment

import com.kyleu.projectile.models.config.UiConfig
import com.kyleu.projectile.util.Logging
import com.kyleu.projectile.util.tracing.TraceData
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

object AugmentService {
  private[this] def htmlErr(x: Throwable) = Some(com.kyleu.projectile.views.html.layout.card(Some("Augmentation Error"))(Html(s"<em>${x.getMessage}</em>")))
  val lists = new AugmentListService[Html](htmlErr)
  val views = new AugmentService[Html](htmlErr)
}

class AugmentService[Rsp](errResponse: Throwable => Option[Rsp]) extends Logging {
  private[this] var map = Map.empty[Class[_], (Any, Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]]]

  def register[T](f: (T, Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]])(implicit tag: ClassTag[T]) = {
    map = map + (tag.runtimeClass -> f.asInstanceOf[(Any, Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]]])
  }

  def augment[T](model: T, args: Map[String, Seq[String]], cfg: UiConfig)(implicit td: TraceData): Option[Rsp] = {
    map.get(model.getClass).flatMap(f => extract(f(model, args, cfg, td)))
  }

  private[this] def extract(f: Future[Option[Rsp]]) = try { Await.result(f, 15.seconds) } catch {
    case NonFatal(x) => errResponse(x)
  }
}

class AugmentListService[Rsp](errResponse: Throwable => Option[Rsp]) extends Logging {
  type Typ = (Seq[Any], Map[String, Seq[String]], UiConfig, TraceData) => Future[(Option[Rsp], Option[Rsp], Map[Any, Option[Rsp]])]
  private[this] var map = Map.empty[Class[_], Typ]

  def register[T](
    f: (Seq[T], Map[String, Seq[String]], UiConfig, TraceData) => Future[(Option[Rsp], Option[Rsp], Map[T, Option[Rsp]])]
  )(implicit tag: ClassTag[T]) = {
    map = map + (tag.runtimeClass -> f.asInstanceOf[Typ])
  }

  def augment[T](models: Seq[T], args: Map[String, Seq[String]], cfg: UiConfig)(implicit td: TraceData): (Option[Rsp], Option[Rsp], Map[T, Option[Rsp]]) = {
    models.headOption match {
      case Some(head) => map.get(head.getClass).map { f =>
        extract(f(models, args, cfg, td))
      }.getOrElse((None, None, Map.empty)).asInstanceOf[(Option[Rsp], Option[Rsp], Map[T, Option[Rsp]])]
      case None => (None, None, Map.empty)
    }
  }

  private[this] def extract[T](f: Future[(Option[Rsp], Option[Rsp], Map[T, Option[Rsp]])]) = try {
    Await.result(f, 15.seconds)
  } catch {
    case NonFatal(x) => (errResponse(x), None, Map.empty)
  }
}
