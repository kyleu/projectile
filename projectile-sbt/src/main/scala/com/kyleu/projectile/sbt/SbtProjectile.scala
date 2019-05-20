package com.kyleu.projectile.sbt

import com.kyleu.projectile.ProjectileCLI
import com.kyleu.projectile.models.cli.CommandLineOutput
import com.kyleu.projectile.models.command.ProjectileResponse
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import sbt.Keys._
import sbt._
import complete.DefaultParsers.spaceDelimited

object SbtProjectile extends AutoPlugin {
  object autoImport {
    val projectile = inputKey[Unit](
      "Generate code from your database, Thrift files, or GraphQL queries using Projectile"
    )
    val projectileCodegen = taskKey[Unit](
      "Runs Projectile, refreshing the generated files in your project"
    )
    val projectileVersion = com.kyleu.projectile.util.Version.version
  }

  override lazy val projectSettings = Seq(
    autoImport.projectile := {
      val streamValue = streams.value
      def log(s: String) = streamValue.log.info(s)
      val args: Seq[String] = spaceDelimited("<args>").parsed

      val svc = new ProjectileService(new ConfigService(baseDirectory.value.getPath))

      try {
        val startMs = System.currentTimeMillis
        val action = ProjectileCLI.parse(args)
        val result = action.map(act => svc.process(act.toCommand, act.verbose))
        result match {
          case Some(r) =>
            val msg = s"Projectile execution with args [${args.mkString(" ")}] completed in [${System.currentTimeMillis - startMs}ms]"
            (msg +: CommandLineOutput.logsFor(r)).foreach(log)
          case None => log("No arguments")
        }
      } catch {
        case x: Throwable => log(s"Error running [${args.mkString(" ")}]: $x")
      }
    },
    autoImport.projectileCodegen := {
      val streamValue = streams.value
      def log(s: String) = streamValue.log.info(s)
      val svc = new ProjectileService(new ConfigService(baseDirectory.value.getPath))
      try {
        val result = svc.codegen(verbose = false)
        val logs = CommandLineOutput.logsFor(ProjectileResponse.ProjectCodegenResult(result))
        logs.foreach(log)
      } catch {
        case x: Throwable => log(s"Error running codegen: $x")
      }
    },
    // sourceGenerators in Compile += Def.task((((sourceManaged in Compile).value / "projectile") ** "*.scala").get).taskValue,
    (compile in Compile) := (compile in Compile).dependsOn(autoImport.projectileCodegen).value
  )
}
