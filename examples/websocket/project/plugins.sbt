scalacOptions ++= Seq( "-unchecked", "-deprecation" )

resolvers += Resolver.typesafeRepo("releases")

// Projectile
addSbtPlugin("com.kyleu" % "projectile-sbt" % "1.5.0")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.21")

// SBT-Web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

// Scala.js
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.26")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6" exclude("org.scala-js", "sbt-scalajs"))
