# Scala

Common classes relating to core models and utilities ([API Documentation](../api/projectile-lib-scala)) 

### Usage

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-scala_2.12"
  version="latest"
}

### Classes

#### com.kyleu.projectile.util.EncryptionUtils

Uses a user-provided secret to encrypt and decrypt arbitrary bytes and strings using AES/ECB/PKCS5Padding

#### com.kyleu.projectile.util.JsonIncludeParser

A JSON parser that supports comments and included external files

#### com.kyleu.projectile.util.Logging

Provides a logging interface that accepts TraceData in its methods and includes the trace ids in its output

#### com.kyleu.projectile.util.tracing.TraceData

Used everywhere, `TraceData` is a common trait that exposes authentication and tracing information for OpenTracing services

#### com.kyleu.projectile.util.tracing.TraceService

Provides tracing helpers and methods to wrap access to OpenTracing
