// Library Projects
lazy val `projectile-lib-core-jvm` = LibraryProjects.`projectile-lib-core-jvm`
lazy val `projectile-lib-core-js` = LibraryProjects.`projectile-lib-core-js`

lazy val `projectile-lib-scala` = LibraryProjects.`projectile-lib-scala`
lazy val `projectile-lib-tracing` = LibraryProjects.`projectile-lib-tracing`

lazy val `projectile-lib-jdbc` = LibraryProjects.`projectile-lib-jdbc`
lazy val `projectile-lib-doobie` = LibraryProjects.`projectile-lib-doobie`
lazy val `projectile-lib-slick` = LibraryProjects.`projectile-lib-slick`

lazy val `projectile-lib-thrift` = LibraryProjects.`projectile-lib-thrift`

lazy val `projectile-lib-graphql` = LibraryProjects.`projectile-lib-graphql`
lazy val `projectile-lib-scalajs` = LibraryProjects.`projectile-lib-scalajs`

lazy val `projectile-lib-admin` = LibraryProjects.`projectile-lib-admin`

// Parser
lazy val `projectile-parser-core` = ParserProjects.`projectile-parser-core`
lazy val `projectile-parser-database` = ParserProjects.`projectile-parser-database`
lazy val `projectile-parser-graphql` = ParserProjects.`projectile-parser-graphql`
lazy val `projectile-parser-thrift` = ParserProjects.`projectile-parser-thrift`
lazy val `projectile-parser-typescript` = ParserProjects.`projectile-parser-typescript`

// Export
lazy val `projectile-export` = ProjectileExport.`projectile-export`

// SBT Plugin
lazy val `projectile-sbt` = SbtExportPlugin.`projectile-sbt`
lazy val `projectile-sbt-admin` = SbtExportPlugin.`projectile-sbt-admin`

// Play Web Application
lazy val `projectile-server` = Server.`projectile-server`

// Sandbox App
lazy val sandbox = Sandbox.sandbox

// Paradox documentation
lazy val doc = Documentation.doc
