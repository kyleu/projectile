package com.projectile.models.cli

import com.projectile.models.command.ProjectileCommand.{StartServer, StopServer}
import com.projectile.models.command.{ProjectileCommand, ProjectileResponse}
import com.projectile.util.NullUtils

object ServerHelper {
  lazy val inst = try {
    Some(getClass.getClassLoader.loadClass("util.web.PlayServerHelper$").getField("MODULE$").get(NullUtils.inst).asInstanceOf[ServerHelper])
  } catch {
    case _: ClassNotFoundException => None
  }
}

trait ServerHelper {
  protected val processServer: PartialFunction[ProjectileCommand, ProjectileResponse] = {
    case StartServer(port) => startServer(port)
    case StopServer => stopServer()
  }

  def startServer(port: Int): ProjectileResponse

  def stopServer(): ProjectileResponse
}
