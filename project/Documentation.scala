import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.sbtghpages.GhpagesPlugin
import com.typesafe.sbt.site.SiteScaladocPlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin
import com.typesafe.sbt.site.paradox.ParadoxSitePlugin.autoImport.Paradox
import sbt.{Project, file}
import sbt.Keys._
import sbt._
import _root_.io.github.jonas.paradox.material.theme.ParadoxMaterialThemePlugin

object Documentation {
  private[this] lazy val docProjects = LibraryProjects.all ++ Seq(
    ProjectExport.`projectile-export`, SbtExportPlugin.`projectile-sbt`
  )

  lazy val doc = Project(id = "doc", base = file("./doc")).settings(Common.settings: _*).settings(docProjects.flatMap(forProject): _*).enablePlugins(
    ParadoxPlugin, ParadoxSitePlugin, SiteScaladocPlugin, GhpagesPlugin, ParadoxMaterialThemePlugin
  ).settings(ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox)).settings(
    sourceDirectory in Paradox := sourceDirectory.value / "main" / "paradox",
    git.remoteRepo := "git@github.com:KyleU/projectile.git",

    paradoxProperties ++= Map(
      "material.color.primary" -> "green",
      "material.color.accent" -> "green",
      "material.logo" -> "settings",
      "material.favicon" -> "logo.png",
      "material.repo" -> "https://github.com/KyleU/projectile",
      "material.repo.type" -> "github",
      "material.repo.name" -> "KyleU/projectile",
      "material.copyright" -> "Kyle Unverferth",
      "material.author" -> "Kyle Unverferth",
      "material.custom.stylesheet" -> "projectile.css"
    )
  )

  private[this] def forProject(project: Project) = {
    val Conf = config(project.id)
    SiteScaladocPlugin.scaladocSettings(
      config = Conf,
      scaladocMappings = mappings in(Compile, packageDoc) in project,
      scaladocDir = s"api/${project.id}"
    )
  }
}
