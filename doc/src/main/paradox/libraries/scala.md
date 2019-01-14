# Scala

Common classes relating to core models and utilities ([API Documentation](../api/projectile-lib-scala)) 

### Usage

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-scala_2.12"
  version="latest"
}

### Classes

#### com.kyleu.projectile.models.result.BaseResult

Wrapper case class used for search results

#### com.kyleu.projectile.models.tag.Tag

A simple key and value case class, used where unordered maps aren't appropriate

#### com.kyleu.projectile.util.DateUtils

Provides ordering, formatting, and common utilities for Local and Zoned `java.time` date classes

#### com.kyleu.projectile.util.EncryptionUtils

Uses a user-provided secret to encrypt and decrypt arbitrary bytes and strings using AES/ECB/PKCS5Padding

#### com.kyleu.projectile.util.JsonIncludeParser

A JSON parser that supports comments and included external files

#### com.kyleu.projectile.util.JsonSerializers

Provides all the imports and utility methods you need to work with Circe using dates, uuids, enums and sealed traits

#### com.kyleu.projectile.util.Logging

Provides a logging interface that accepts TraceData in its methods and includes the trace ids in its output

#### com.kyleu.projectile.util.NullUtils

Rather than use `null` references in your code, NullUtils is provided for common operations and values involving nulls

#### com.kyleu.projectile.util.NumberUtils

Currently only provides a cross-platform method for formatting numbers

#### com.kyleu.projectile.util.StringUtils

Exposes a method to split a string to an ordered sequence of lines

#### com.kyleu.projectile.util.UuidUtils

Utility methods to transform a `java.util.UUID` to and from a sequence of bytes

#### com.kyleu.projectile.util.tracing.TraceData

Used everywhere, `TraceData` is a common trait that exposes authentication and tracing information for OpenTracing services

#### com.kyleu.projectile.util.tracing.TraceService

Provides tracing helpers and methods to wrap access to OpenTracing
