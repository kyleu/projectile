import sbt.Keys._
import sbt._

object LibraryProjects {
  lazy val `projectile-lib-scala` = (project in file("libraries/projectile-lib-scala")).settings(Shared.commonSettings: _*).settings(
    name := "projectile-lib-scala",
    description := "Common classes used by code generated from Projectile",
    libraryDependencies ++= Dependencies.Serialization.all :+ Dependencies.Utils.enumeratum,
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Shared.projectId, Shared.projectName, Shared.projectPort).taskValue,
  )
}
