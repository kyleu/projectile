# First Steps

This tutorial will lead you through creating a real-time web application that facilitates [planning poker](https://en.wikipedia.org/wiki/Planning_poker) sessions.

The basic idea is to create a PostgreSQL database, use that to have Projectile generate some code, and then repeat as we add features. 

With luck, we'll end up with a useful web application, identical to the version available [here](https://estimate.poker)

## Clone the repo

Projectile goes to great lengths to minimize the amount of boilerplate you need to provide in your codebase, but there's still a fair bit of ceremony involved in starting a new sbt project.
To save us both some typing, go ahead and clone the repository for this tutorial

```shell
git clone --branch tutorial-1 https://github.com/KyleU/estimate.poker.git
```

## Explore the project

There's only a few files in the project, so let's take a look at them:

#### project/[plugins.sbt](https://github.com/KyleU/estimate.poker/blob/master/project/plugins.sbt)

Adds the Projectile sbt plugin, which runs on compile to update any generated code

@@@vars
```
addSbtPlugin("com.kyleu" % "projectile-sbt" % "$project.version$")
```
@@@

#### [build.sbt](https://github.com/KyleU/estimate.poker/blob/master/build.sbt)

Adds the Projectile scala library as a dependency, for common utilities and helper classes

@@@vars
```
libraryDependencies += "com.kyleu" %% "projectile-lib-scala" % "$project.version$"
```
@@@

Adds the projectile plugin to the project

@@@vars
```
enablePlugins(SbtProjectile)
```
@@@

The rest of the files are common sbt boilerplate, setting the correct sbt and Java version, and adding a `.gitignore` 

## Run the project

From the directory you cloned the project to, run `sbt` (you'll need sbt installed, obviously). From the interactive command prompt, you can run tasks like `compile` and `publish`.

If you `run` the project, `Entrypoint` will be called, which print's the obligatory "Hello World".

There's not much to be impressed by yet, so let's @ref[move on](02_database-setup.md)!
