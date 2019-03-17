import sbt._

object Dependencies {
  object Projectile {
    val version = "1.5.0"
    val all = Seq("websocket", "slick").map(s => "com.kyleu" %% s"projectile-lib-$s" % version)
  }

  object PlayFramework {
    val filters = play.sbt.PlayImport.filters
    val cache = play.sbt.PlayImport.ehcache
    val guice = play.sbt.PlayImport.guice
    val json = "com.typesafe.play" %% "play-json" % "2.6.13"
    val ws = play.sbt.PlayImport.ws

    val all = Seq(filters, guice, ws, json, cache)
  }
}
