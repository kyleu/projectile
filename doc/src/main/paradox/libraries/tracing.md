# Tracing

Helper classes for OpenTracing and Micrometer ([API Documentation](../api/projectile-lib-tracing))

### Usage

@@@vars

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-tracing_2.12"
  version="$project.version$"
}

@@@

### Classes

#### [Instrumented](../api/projectile-lib-tracing/com/kyleu/projectile/util/metrics/Instrumented$.html)

Helper class to initialize Prometheus, Datadog, or StatsD metrics reporting

#### [MetricsConfig](../api/projectile-lib-tracing/com/kyleu/projectile/util/metrics/MetricsConfig.html)

Case class containing all of the information needed to report metrics. Usually created from a config file.

#### [OpenTracingService](../api/projectile-lib-tracing/com/kyleu/projectile/util/tracing/OpenTracingService.html)

Implements TracingService to provide system-wide tracing support

#### [TraceDataOpenTracing](../api/projectile-lib-tracing/com/kyleu/projectile/util/tracing/TraceDataOpenTracing.html)

Extends TraceData with OpenTracing-specific functionality
