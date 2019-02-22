# Getting Started

There are several ways to run Projectile:

## Release Jar

- Download the leatest release from [GitHub](https://github.com/Kyleu/projectile/releases). Only a single file, `projectile.jar`, is needed

- From your project's directory, run `java -jar projectile.jar`, this will print CLI usage

- To run the web server, run `java -jar projectile.jar server`


## Shell script

Save [this file](https://raw.githubusercontent.com/KyleU/projectile/master/bin/projectile.sh) as `projectile.sh` and mark it executable. 
Once executed, it will download and run the latest release from GitHub


## Ammonite

Save [this file](https://raw.githubusercontent.com/KyleU/projectile/master/bin/projectile.sc) as `projectile.sc`. 
Run with `amm projectile.sc` and it will download the dependencies


## SBT Plugin

An SBT plugin (@ref[details here](codegen/sbt-plugin.md)) is provided for running Projectile from within your project's SBT session


## Run From Source

Clone [Projectile](https://github.com/Kyleu/projectile)

```shell
$ cd projectile
$ sbt
> run
$ open http://127.0.0.1:20000
```

The project is built on Scala and SBT, and can be opened by IntelliJ directly
