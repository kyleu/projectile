package com.kyleu.projectile.sbt

import java.io.Closeable

import play.sbt.PlayNonBlockingInteractionMode
import sbt.util.{Level, Logger}

object PlayUtils {
  object NonBlockingInteractionMode extends PlayNonBlockingInteractionMode {
    object NullLogger extends Logger {
      def trace(t: => Throwable): Unit = ()
      def success(message: => String): Unit = ()
      def log(level: Level.Value, message: => String): Unit = ()
    }

    private[this] var runningServers: Set[Closeable] = scala.collection.immutable.HashSet.empty
    private[this] def log(s: String) = println(s) // scalastyle:ignore

    override def start(server: => Closeable): Unit = synchronized {
      val theServer = server
      System.clearProperty("play.server.http.port")
      if (runningServers(theServer)) {
        log("Noop: This server was already started")
      } else {
        runningServers += theServer
      }
    }

    override def stop(): Unit = synchronized {
      if (runningServers.size > 1) {
        log("Stopping all servers")
      } else if (runningServers.size == 1) {
        log("Stopping server")
      } else {
        log("No running server to stop")
      }
      runningServers.foreach(_.close())
      runningServers = scala.collection.immutable.HashSet.empty
    }
  }
}
