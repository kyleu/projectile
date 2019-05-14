scalacOptions ++= Seq( "-unchecked", "-deprecation" )

// Projectile codegen
addSbtPlugin("com.kyleu" % "projectile-sbt" % "1.8.4")

// Web
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")
addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.4.13")

// Scala.js
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.5.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6" exclude("org.scala-js", "sbt-scalajs"))

// Source Control
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
