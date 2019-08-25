# Scala

Common classes relating to core models and utilities
([API Documentation](../api/projectile-lib-scala/com/kyleu/projectile/index.html)) 

### Usage

@@@vars

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-scala_2.12"
  version="$project.version$"
}

@@@

### Classes

#### [EncryptionUtils](../api/projectile-lib-scala/com/kyleu/projectile/util/EncryptionUtils$.html)

Uses a user-provided secret to encrypt and decrypt arbitrary bytes and strings using AES/ECB/PKCS5Padding

#### [JsonIncludeParser](../api/projectile-lib-scala/com/kyleu/projectile/util/JsonIncludeParser.html)

A JSON parser that supports comments and included external files

#### [Logging](../api/projectile-lib-scala/com/kyleu/projectile/util/Logging.html)

Provides a logging interface that accepts TraceData in its methods and includes the trace ids in its output

#### [TraceData](../api/projectile-lib-scala/com/kyleu/projectile/util/tracing/TraceData.html)

Used everywhere, `TraceData` is a common trait that exposes authentication and tracing information for OpenTracing services

#### [TracingService](../api/projectile-lib-scala/com/kyleu/projectile/util/tracing/TracingService.html)

Provides tracing helpers and methods to wrap access to OpenTracing
