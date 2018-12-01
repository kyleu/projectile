package com.projectile

import sbt._
import sbt.Keys._
import complete.DefaultParsers.spaceDelimited

object SbtProjectile extends AutoPlugin {
  object autoImport {
    val projectile = inputKey[Unit]("Generate better code from your database, Thrift files, or GraphQL queries using Projectile")
  }

  override lazy val projectSettings = inConfig(Test)(projectileSettings) ++ inConfig(Compile)(projectileSettings)

  private[this] val projectileSettings: Seq[Setting[_]] = Seq(
    autoImport.projectile := {
      val streamValue = streams.value
      def log(s: String) = streamValue.log.info(s)
      val args: Seq[String] = spaceDelimited("<args>").parsed

      try {
        val startMs = System.currentTimeMillis
        ProjectileCLI.runArgs(args) match {
          case Some(result) =>
            log(s"Code generation completed in [${System.currentTimeMillis - startMs}ms]")
            log(result.toString)
          case None =>
            log("No arguments")
        }
      } catch {
        case x: Throwable =>
          // x.printStackTrace()
          log(s"Error running [${args.mkString(" ")}]: $x")
      }
    }
  )
}
