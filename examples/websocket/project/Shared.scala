import com.kyleu.projectile.sbt.ProjectVersion

import sbt.Keys._
import sbt._

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType, _}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._

object Shared {
  lazy val shared = (crossProject(JSPlatform, JVMPlatform).withoutSuffixFor(JVMPlatform).crossType(CrossType.Pure) in file("shared")).settings(
    libraryDependencies += "com.kyleu" %%% "projectile-lib-core" % Dependencies.Projectile.version,
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Common.projectId, Common.projectName, Common.projectPort).taskValue
  ).settings(Common.commonSettings: _*)

  lazy val sharedJs = shared.js
  lazy val sharedJvm = shared.jvm
}
