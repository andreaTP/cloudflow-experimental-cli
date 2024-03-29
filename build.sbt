name := "kubectl-cloudflow_exp"

scalaVersion := "2.13.6"

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-target:jvm-1.8",
  "-Xlog-reflective-calls",
  "-Xlint",
  "-Wunused:imports",
  "-deprecation",
  "-feature",
  "-language:_",
  "-unchecked"
  // TODO: re-enable this after cleanup
  // "-Xfatal-warnings"
)

scalafmtOnCompile := true

resolvers += Resolver.bintrayRepo("astrac", "maven")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.0",
  "io.quarkus" % "quarkus-kubernetes-client" % "1.9.0.Final" exclude ("org.jboss.slf4j", "slf4j-jboss-logging"),
  "com.github.alexarchambault" %% "case-app" % "2.0.4",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.wvlet.airframe" %% "airframe-log" % "20.10.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.3", // matches the provided one
  "com.typesafe.play" %% "play-json" % "2.9.1",
    "org.scalatest" %% "scalatest" % "3.2.0" % Test,
  "io.fabric8" % "kubernetes-server-mock" % "4.12.0" % Test
)

enablePlugins(BuildInfoPlugin, GraalVMNativeImagePlugin)
// this will build the Linux binaries on docker
// TODO: 20.2.0 doesn't work because of: https://github.com/oracle/graal/issues/2826
// downgrading to 20.1 works
// graalVMNativeImageGraalVersion := Some("20.2.0-java11")

graalVMNativeImageOptions := Seq(
  "--verbose",
  "--no-server",
  "--enable-http",
  "--enable-https",
  "--enable-url-protocols=http,https,file,jar",
  "--enable-all-security-services",
  "-H:+JNI",
  // "--static", not supported on Mac
  // TODO integrate musl for linux
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:+ReportExceptionStackTraces",
  "--no-fallback",
  "--initialize-at-build-time",
  "--report-unsupported-elements-at-runtime",
  // DEBUG possibly to be removed
  "--allow-incomplete-classpath",
  // a possibilty
  // "--delay-class-initialization-to-runtime=",
  "--initialize-at-run-time" + Seq(
    "com.typesafe.config.impl.ConfigImpl",
    "com.typesafe.config.impl.ConfigImpl$EnvVariablesHolder",
    "com.typesafe.config.impl.ConfigImpl$SystemPropertiesHolder",
    "com.typesafe.config.impl.ConfigImpl$LoaderCacheHolder"
  ).mkString("=", ",", "")
)

fork in run := true

// Command used to generate the configuration for the reflection
// fork in run := true
run / javaOptions += "-agentlib:native-image-agent=config-output-dir=./tmp"
