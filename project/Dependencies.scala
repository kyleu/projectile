import sbt._

object Dependencies {
  object Play {
    private[this] val version = "2.6.20"
    val lib = "com.typesafe.play" %% "play" % version
    val filters = play.sbt.PlayImport.filters
    val ws = play.sbt.PlayImport.ws
    val guice = play.sbt.PlayImport.guice
    val cache = play.sbt.PlayImport.ehcache
    val json = "com.typesafe.play" %% "play-json" % "2.6.10"
    val test = "com.typesafe.play" %% "play-test" % version % "test"
  }

  object Akka {
    private[this] val version = "2.5.17"
    val actor = "com.typesafe.akka" %% "akka-actor" % version
    val remote = "com.typesafe.akka" %% "akka-remote" % version
    val logging = "com.typesafe.akka" %% "akka-slf4j" % version
    val cluster = "com.typesafe.akka" %% "akka-cluster" % version
    val clusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % version
    val clusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % version
    val protobuf = "com.typesafe.akka" %% "akka-protobuf" % version
    val stream = "com.typesafe.akka" %% "akka-stream" % version
    val visualMailbox = "de.aktey.akka.visualmailbox" %% "collector" % "1.1.0"
    val testkit = "com.typesafe.akka" %% "akka-testkit" % version % "test"
  }

  object Database {
    val postgres = "org.postgresql" % "postgresql" % "42.2.5"
  }

  object GraphQL {
    val sangria = "org.sangria-graphql" %% "sangria" % "1.4.2"
    val playJson = "org.sangria-graphql" %% "sangria-play-json" % "1.0.4"
    val circe = "org.sangria-graphql" %% "sangria-circe" % "1.2.1"
  }

  object Serialization {
    private[this] val version = "0.10.0"
    private[this] val projects = Seq("circe-core", "circe-generic", "circe-generic-extras", "circe-parser", "circe-java8")
    val all = projects.map(c => "io.circe" %% c % version)
  }

  object WebJars {
    val fontAwesome = "org.webjars" % "font-awesome" % "4.7.0" intransitive()
    val jquery = "org.webjars" % "jquery" % "2.2.4" intransitive()
    val materialize = "org.webjars" % "materializecss" % "1.0.0" intransitive()
  }

  object Utils {
    val scapegoatVersion = "1.3.8"

    val betterFiles = "com.github.pathikrit" %% "better-files" % "3.6.0"
    val chimney = "io.scalaland" %% "chimney" % "0.2.1"
    val commonsIo = "commons-io" % "commons-io" % "2.6"
    val commonsLang = "org.apache.commons" % "commons-lang3" % "3.8.1"
    val enumeratum = "com.beachape" %% "enumeratum-circe" % "1.5.18"
    val scalaGuice = "net.codingwell" %% "scala-guice" % "4.2.1"
    val schwatcher = "com.beachape.filemanagement" %% "schwatcher" % "0.3.5"
    val clist = "org.backuity.clist" %% "clist-core"   % "3.4.0"
    val clistMacros = "org.backuity.clist" %% "clist-macros" % "3.4.0" % "provided"
  }

  object Testing {
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"
  }
}
