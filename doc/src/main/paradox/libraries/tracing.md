# Tracing

Helper classes for OpenTracing and Micrometer ([API Documentation](../api/projectile-lib-tracing))

### Usage

@@dependency[sbt,Maven,Gradle] {
  group="com.kyleu"
  artifact="projectile-lib-tracing_2.12"
  version="latest"
}

### Classes

#### com.kyleu.projectile.util.metrics.Instrumented

Helper class to initialize Prometheus, Datadog, or StatsD metrics reporting

#### com.kyleu.projectile.util.metrics.MetricsConfig

Case class containing all of the information needed to report metrics. Usually created from a config file.

#### com.kyleu.projectile.util.tracing.OpenTracingService

Implements TracingService to provide system-wide tracing support

#### com.kyleu.projectile.util.tracing.TraceDataOpenTracing

Extends TraceData with OpenTracing-specific functionality
