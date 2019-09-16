package com.kyleu.projectile.services.augment

import com.kyleu.projectile.models.config.UiConfig
import com.kyleu.projectile.util.{ExceptionUtils, Logging}
import com.kyleu.projectile.util.tracing.TraceData
import play.twirl.api.Html

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag
import scala.util.control.NonFatal

object AugmentService extends Logging {
  private[this] def htmlErr(ex: Throwable) = {
    log.warn(s"Error running augmentation", ex)(TraceData.noop)
    Some(com.kyleu.projectile.views.html.layout.card(Some("Augmentation Error")) {
      Html(s"<em>${ex.getMessage}</em>\n<!--\n\n${ExceptionUtils.print(ex)}\n\n-->")
    })
  }
  val listHeaders = new AugmentClassService[Html](htmlErr)
  val lists = new AugmentListService[Html](htmlErr)
  val viewHeaders = new AugmentService[Html](htmlErr)
  val viewDetails = new AugmentService[Html](htmlErr)
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

class AugmentClassService[Rsp](errResponse: Throwable => Option[Rsp]) extends Logging {
  private[this] var map = Map.empty[Class[_], (Any, Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]]]

  def register(cls: Class[_], f: (Class[_], Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]]) = {
    map = map + (cls -> f.asInstanceOf[(Any, Map[String, Seq[String]], UiConfig, TraceData) => Future[Option[Rsp]]])
  }

  def augment(cls: Class[_], args: Map[String, Seq[String]], cfg: UiConfig)(implicit td: TraceData): Option[Rsp] = {
    map.get(cls).flatMap(f => extract(f(cls, args, cfg, td)))
  }

  private[this] def extract(f: Future[Option[Rsp]]) = try { Await.result(f, 15.seconds) } catch {
    case NonFatal(x) => errResponse(x)
  }
}

class AugmentListService[Rsp](errResponse: Throwable => Option[Rsp]) extends Logging {
  type Typ = (Seq[Any], Map[String, Seq[String]], UiConfig, TraceData) => Future[(Option[Rsp], Map[Any, Option[Rsp]])]
  private[this] var map = Map.empty[Class[_], Typ]

  def register[T](
    f: (Seq[T], Map[String, Seq[String]], UiConfig, TraceData) => Future[(Option[Rsp], Map[T, Option[Rsp]])]
  )(implicit tag: ClassTag[T]) = {
    map = map + (tag.runtimeClass -> f.asInstanceOf[Typ])
  }

  def augment[T](models: Seq[T], args: Map[String, Seq[String]], cfg: UiConfig)(implicit td: TraceData): (Option[Rsp], Map[T, Option[Rsp]]) = {
    models.headOption match {
      case Some(head) => map.get(head.getClass).map { f =>
        extract(f(models, args, cfg, td))
      }.getOrElse((None, Map.empty)).asInstanceOf[(Option[Rsp], Map[T, Option[Rsp]])]
      case None => (None, Map.empty)
    }
  }

  private[this] def extract[T](f: Future[(Option[Rsp], Map[T, Option[Rsp]])]) = try {
    Await.result(f, 15.seconds)
  } catch {
    case NonFatal(x) => (errResponse(x), Map.empty)
  }
}
