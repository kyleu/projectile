# Getting Started

There are several ways to run Projectile:

## SBT Plugin

- Add the following to your `plugins.sbt`: 

@@@vars
```
addSbtPlugin("com.kyleu" % "projectile-sbt" % "$project.version$")
```
@@@

- Now, projectile will run each time you compile your project (don't worry, it only adds a few milliseconds)


## Release Jar

- Download the leatest release from [Github](https://github.com/Kyleu/projectile/releases). Only a single file, `projectile.jar`, is needed

- From your projects directory, run `java -jar projectile.jar`, this will print CLI usage

- To run the web server, run `java -jar projectile.jar server`


## Run From Source

- Clone [Projectile](https://github.com/Kyleu/projectile)

```shell
$ cd projectile
$ sbt
> run
$ open http://127.0.0.1:20000
```

The project is built on Scala and SBT, and can be opened by IntelliJ directly
