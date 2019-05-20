scalacOptions ++= Seq( "-unchecked", "-deprecation" )

// Projectile codegen
addSbtPlugin("com.kyleu" % "projectile-sbt-admin" % "_PROJECTILE_VERSION")

// Source Control
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
