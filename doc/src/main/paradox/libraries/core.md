# Core

Classes and utilities shared between Scala and Scala.js ([API Documentation](../api/core)) 

### Usage

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-core_2.12"
  version="latest"
}

### Classes

#### com.kyleu.projectile.models.result.BaseResult

Wrapper case class used for search results

#### com.kyleu.projectile.models.tag.Tag

A simple key and value case class, used where unordered maps aren't appropriate

#### com.kyleu.projectile.util.DateUtils

Provides ordering, formatting, and common utilities for Local and Zoned `java.time` date classes

#### com.kyleu.projectile.util.JsonSerializers

Provides all the imports and utility methods you need to work with Circe using dates, uuids, enums and sealed traits

#### com.kyleu.projectile.util.NullUtils

Rather than use `null` references in your code, NullUtils is provided for common operations and values involving nulls

#### com.kyleu.projectile.util.NumberUtils

Currently only provides a cross-platform method for formatting numbers

#### com.kyleu.projectile.util.StringUtils

Exposes a method to split a string to an ordered sequence of lines

#### com.kyleu.projectile.util.UuidUtils

Utility methods to transform a `java.util.UUID` to and from a sequence of bytes
