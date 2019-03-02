# Installing Projectile

You've got a few options for acquiring Projectile. Most folks use the shell script, but other methods are available. 
If you're looking to extend or contribute to Projectile, run it from source.  


## Option 1: Shell script

Save [this file](https://raw.githubusercontent.com/KyleU/projectile/master/bin/projectile.sh) as `projectile.sh` and mark it executable. 
Once executed, it will download and run the latest release from GitHub, printing usage instructions by default.
Run `projectile.sh server` to start the server


## Option 2: Release jar

- [Download the latest release]((https://github.com/Kyleu/projectile/releases)) from GitHub. Only a single file, `projectile.jar`, is needed
- From your project's directory, run `java -jar projectile.jar`, this will print CLI usage
- To run the web server, run `java -jar projectile.jar server`


## Option 3: Ammonite

Save [this file](https://raw.githubusercontent.com/KyleU/projectile/master/bin/projectile.sc) as `projectile.sc`. 
Run with `amm projectile.sc` and it will download the dependencies and print usage instructions. 
Run `amm projectile.sc server` to start the server


## Option 4: SBT plugin

An SBT plugin (@ref[details here](../codegen/sbt-plugin.md)) is provided for running Projectile from within your project's SBT session.
Because of SBT plugin restrictions, the web server is unavailable, though command-line usage is supported. 
You can use one of the other methods to launch the server and use a UI to configure your project


## Option 5: Run from source

Clone [Projectile](https://github.com/Kyleu/projectile)

```shell
$ cd projectile
$ sbt
> run
$ open http://127.0.0.1:20000
```

The project is built on Scala and SBT, and can be opened by IntelliJ directly
