# SBT Plugin

Projectile provides a (completely optional) sbt plugin

The plugin provides the command-line version of Projectile inside your application's build. 
If you've installed Projectile via homebrew or one of the other methods, you don't need the sbt plugin. 

To enable it, add the following to your `plugins.sbt`: 

@@@vars
```
addSbtPlugin("com.kyleu" % "projectile-sbt" % "$project.version$")
```
@@@

Then add it to your project's build definition: 

```
enablePlugins(SbtProjectile)
```

Now, projectile will run each time you compile your project (don't worry, it only adds a few milliseconds).
You can also use the CLI by running the sbt task `projectile`

[API Documentation](../api/projectile-sbt/com/kyleu/projectile/index.html)


