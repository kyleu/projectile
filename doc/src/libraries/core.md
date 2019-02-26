# Core

Classes and utilities shared between Scala and Scala.js ([API Documentation](../api/projectile-lib-core/com/kyleu/projectile/index.html)) 

### Usage

@@@vars

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-core_2.12"
  version="$project.version$"
}

@@@

(Or, use "%%%" for Scala.js and cross-built projects)


### Classes

#### [BaseResult](../api/core/com/kyleu/projectile/models/result/BaseResult.html)

Wrapper case class used for search results

#### [Tag](../api/core/com/kyleu/projectile/models/tag/Tag.html)

A simple key and value case class, used where unordered maps aren't appropriate

#### [DateUtils](../api/core/com/kyleu/projectile/util/DateUtils$.html)

Provides ordering, formatting, and common utilities for Local and Zoned `java.time` date classes

#### [JsonSerializers](../api/core/com/kyleu/projectile/util/JsonSerializers$.html)

Provides all the imports and utility methods you need to work with Circe using dates, uuids, enums and sealed traits

#### [NullUtils](../api/core/com/kyleu/projectile/util/NullUtils$.html)

Rather than use `null` references in your code, NullUtils is provided for common operations and values involving nulls

#### [NumberUtils](../api/core/com/kyleu/projectile/util/NumberUtils$.html)

Currently only provides a cross-platform method for formatting numbers

#### [StringUtils](../api/core/com/kyleu/projectile/util/StringUtils$.html)

Exposes a method to split a string to an ordered sequence of lines

#### [UuidUtils](../api/core/com/kyleu/projectile/util/UuidUtils$.html)

Utility methods to transform a `java.util.UUID` to and from a sequence of bytes
