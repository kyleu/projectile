package services.config

import better.files.File
import util.JsonSerializers._

abstract class FileCacheService[A: Decoder: Encoder: Ordering](val dir: File, modelType: String) {
  def newModel(key: String, title: String, description: String): A

  private[this] val cache = collection.mutable.HashMap.empty[String, A]

  def refresh(key: Option[String] = None) = key match {
    case Some(k) =>
      cache(k) = load(k)
      Seq(cache(k))
    case None =>
      dir.children.filter(_.isDirectory).foreach { x =>
        val k = x.name.stripSuffix(".json").trim
        cache(k) = load(k)
      }
      list()
  }

  def list() = cache.values.toSeq.sorted

  def get(key: String) = cache.getOrElseUpdate(key, load(key))

  protected[this] def load(key: String) = {
    val f = dir / key / s"$key.json"
    if (f.exists && f.isRegularFile && f.isReadable) {
      decodeJson[A](f.contentAsString) match {
        case Right(p) => p
        case Left(x) => newModel(key, s"$modelType Load Error", x.getMessage)
      }
    } else {
      newModel(key, s"New $modelType", "...")
    }
  }
}
