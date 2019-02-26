# Scala.js

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `07-admin-site` branch

@@@


It sure would be nice if we could use the same models from our database in the client. 
Luckily, [Scala.js](https://www.scala-js.org/) was built to solve this exact problem! We'll be adding a `client` project, as well as a `shared` project for, well, shared code.


## Shared project structure

To start, let's add the following two files to `/project`:

`project/Shared.scala` 

```scala
object Shared {
  lazy val shared = (crossProject(JSPlatform, JVMPlatform).withoutSuffixFor(JVMPlatform).crossType(CrossType.Pure) in file("shared")).settings(
    libraryDependencies += "com.kyleu" %%% "projectile-lib-core" % Dependencies.Projectile.version,
    (sourceGenerators in Compile) += ProjectVersion.writeConfig(Common.projectId, Common.projectName, Common.projectPort).taskValue
  ).settings(Common.commonSettings: _*)

  lazy val sharedJs = shared.js
  lazy val sharedJvm = shared.jvm
}
```

`project/Client.scala` 

```scala
object Client {
  private[this] val clientSettings = Common.commonSettings ++ Seq(
    libraryDependencies ++= Seq("com.kyleu" %%% "projectile-lib-scalajs" % Dependencies.Projectile.version)
  )

  lazy val client = (project in file("client")).settings(clientSettings: _*).enablePlugins(ScalaJSPlugin, ScalaJSWeb).dependsOn(Shared.sharedJs)
}
```

We'll need to change the definition of `Server` to include our new `client` project:

`project/Server.scala`

```scala
    ...
    // Scala.js
    scalaJSProjects := Seq(Client.client),

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages ++= Seq(gzip),
    ...

```

And, since we've got two new projects (three, if you count sharedJs and sharedJvm separately), add them to `build.sbt`:

```scala
lazy val sharedJvm = Shared.sharedJvm
lazy val sharedJs = Shared.sharedJs
lazy val client = Client.client
lazy val `estimate-poker` = Server.`estimate-poker`
```


## Moving generated files

The generated models are still in the `server` project, so we'll need to move them to `shared`. 
Projectile has support for this, just edit your project (the "Edit Summary" link on the detail page), and change the template from "Play Framework" to "Scala.js".

Export your project again, and it will store the generated models in `shared/src/main/scala`. 
To remove the now-unreferenced files from the server project, simply "Audit" your project, and "fix all" the orphaned files


## Use what you've built

Restart sbt, and your admin site should now include JavaScript enhancements like form editors and autocomnplete. 
See the docs on @ref[projectile-lib-core](../../libraries/core.md) and @ref[projectile-lib-scalajs](../../libraries/scalajs.md) for more details on the new libraries we're using.


## Explore the code

https://github.com/KyleU/estimate/tree/08-scala-js

See this branch's Pull Request for detailed comments on the modified files

https://github.com/KyleU/estimate/pull/8


## Next steps

Now that we've (finally) got a full-featured web application using classes shared between the JVM and JavaScript, it's time to build a real planning poker application! @ref[Come on, let's go](09-websocket.md)!
