#!/bin/bash
set -e

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $script_dir

native-image \
  --verbose \
  -H:IncludeResources=.*properties \
  -H:IncludeResources=.*conf \
  -H:IncludeResources=.*xml \
  -H:IncludeResources=.*routes \
  -H:+ReportExceptionStackTraces \
  -H:+JNI \
  --enable-url-protocols=https,http \
  -H:ReflectionConfigurationFiles=reflect.json \
  --no-fallback \
  --initialize-at-build-time=scala.Function1 \
  --initialize-at-build-time=scala.Function2 \
  --allow-incomplete-classpath \
  --initialize-at-build-time=scala.Symbol \
  --initialize-at-build-time=scala.runtime.LambdaDeserialize \
  --initialize-at-build-time=scala.runtime.LambdaDeserializer$ \
  --initialize-at-build-time=org.postgresql \
  -jar ../../projectile-export/target/scala-2.12/projectile-export.jar
