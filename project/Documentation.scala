import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport.Paradox
import sbt.{ Project, file }
import sbt.Keys._
import sbt._

object Documentation {
  private[this] lazy val docProjects = LibraryProjects.all ++ Seq(
    ProjectExport.`projectile-export`, SbtExportPlugin.`projectile-sbt`
  )

  lazy val doc = Project(id = "doc", base = file("./doc")).enablePlugins(
    ParadoxPlugin, ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin).settings(Common.settings: _*).settings(
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    sourceDirectory in Paradox := sourceDirectory.value / "main" / "paradox",
    git.remoteRepo := "git@github.com:KyleU/projectile.git"
  ).settings(docProjects.flatMap(forProject): _*)

  private[this] def forProject(project: Project) = {
    val Conf = config(project.id)
    SiteScaladocPlugin.scaladocSettings(
      config = Conf,
      scaladocMappings = mappings in(Compile, packageDoc) in project,
      scaladocDir = s"api/${project.id}"
    )
  }
}
