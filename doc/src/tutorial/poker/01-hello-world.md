# Hello World

This tutorial will lead you through creating a real-time web application that facilitates [planning poker](https://en.wikipedia.org/wiki/Planning_poker) sessions.

The basic idea is to create a PostgreSQL database, use that to have Projectile generate some code, and then repeat as we add features.

With luck, we'll end up with a useful web application, identical to the version available [here](https://estimate.poker)


## Clone the repo

Projectile goes to great lengths to minimize the amount of boilerplate you need to provide in your codebase, but there's still a fair bit of ceremony involved in starting a new sbt project.
To save us both some typing, go ahead and clone the repository for this tutorial

```shell
git clone --branch 01-hello-world https://github.com/KyleU/estimate.git
```

If you've cloned the Projectile repo prior to following this tutorial, checkout this tutorial step's branch

```shell
git checkout --branch 01-hello-world
```


## Run the project

From the directory you cloned the project to, run `sbt` (you'll need sbt installed, obviously). From the interactive command prompt, you can run tasks like `compile` and `publish`.

If you `run` the project, `Entrypoint` will be called, which print's the obligatory "Hello World". 
You can also run the sbt task by calling `projectile`, which will print the usage instructions


## Explore the code

https://github.com/KyleU/estimate/tree/01-hello-world   

See this branch's Pull Request for detailed comments on the code

https://github.com/KyleU/estimate/pull/1


## Next steps

There's not much to be impressed by yet, so let's @ref[move on](02-database-setup.md)!
