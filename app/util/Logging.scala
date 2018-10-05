package util

import org.slf4j.{LoggerFactory, MarkerFactory}
import play.api.{Logger, MarkerContext}

object Logging {
  private[this] val metricsId = util.Config.metricsId + "_logging"

  final case class CustomLogger(name: String) extends Logger(LoggerFactory.getLogger(name)) {
    implicit val mc: MarkerContext = MarkerContext(MarkerFactory.getMarker(name))

    def errorThenThrow(message: => String)(implicit mc: play.api.MarkerContext) = {
      this.error(message)
      throw new IllegalStateException(message)
    }
    def errorThenThrow(message: => String, error: => Throwable)(implicit mc: play.api.MarkerContext) = {
      this.error(message, error)
      throw error
    }
  }
}

trait Logging {
  protected[this] val log = Logging.CustomLogger(s"${util.Config.projectId}.${this.getClass.getSimpleName.replace("$", "")}")
}
