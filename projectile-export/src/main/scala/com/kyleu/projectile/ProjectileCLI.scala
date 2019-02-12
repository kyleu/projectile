package com.kyleu.projectile

import com.kyleu.projectile.models.cli.CommandLineAction
import com.kyleu.projectile.services.ProjectileService
import com.kyleu.projectile.services.config.ConfigService
import com.kyleu.projectile.util.{Logging, StringUtils}
import com.kyleu.projectile.util.Version.{projectId, projectName, version}
import org.backuity.clist.{Cli, ParsingException}

object ProjectileCLI extends Logging {
  def runArgs(args: Seq[String]) = parse(args).map { c =>
    new ProjectileService(new ConfigService(c.dir)).process(c.toCommand, c.verbose)
  }

  def runBatch(args: Array[String]) = {
    if (args.length != 2) {
      throw new IllegalStateException("When calling batch, please pass only a single path as the argument")
    }
    val f = better.files.File(args(1))
    if (f.exists && f.isRegularFile && f.isReadable) {
      f.lines.toList.map(l => l -> runArgs(StringUtils.toList(l, ' ')))
    } else {
      throw new IllegalStateException(s"Cannot read batch file [${f.pathAsString}]")
    }
  }

  def parse(args: Seq[String]): Option[CommandLineAction] = try {
    Cli.parse(args.toArray).withProgramName(projectId).version(version, projectName).throwExceptionOnError().withCommands(CommandLineAction.actions: _*)
  } catch {
    case _: ParsingException => None
  }
}
