ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val catsVersion = "2.9.0"
val catsEffect3Version = "3.4.8"
val circeVersion = "0.14.5"
val sttpClientVersion = "3.9.0"
val testVersion = "1.4.0"
val mockitoVersion = "3.2.16.0"
val catsBackendVersion = "3.8.13"
val configVersion = "1.4.2"
val ficusVersion = "1.5.2"
val wireVersion = "2.5.8"
val wireMockVersion = "3.0.0"
val testContainersVersion = "0.40.15"
val catsRetryVersion = "3.1.0"
val catsLoggingVersion = "2.6.0"
val tapirVersion = "1.7.6"
val tethysVersion = "0.26.0"
val http4sVersion = "0.23.23"
val doobieVersion = "1.0.0-RC2"
val quillVersion = "4.6.0"
val pureConfigVersion = "0.17.4"
val flywayVersion = "9.16.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "pet-store-client",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffect3Version,
      "org.typelevel" %% "cats-effect-testing-scalatest" % testVersion % Test,
      "com.softwaremill.sttp.client3" %% "core" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "circe" % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % catsBackendVersion,
      "com.github.cb372" %% "cats-retry" % catsRetryVersion,
      "org.typelevel" %% "log4cats-core" % catsLoggingVersion,
      "org.typelevel" %% "log4cats-slf4j" % catsLoggingVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.scalatestplus" %% "mockito-4-11" % mockitoVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion,
      "com.typesafe" % "config" % configVersion,
      "com.iheart" %% "ficus" % ficusVersion,
      "com.softwaremill.macwire" %% "macros" % wireVersion % Provided,
      "com.softwaremill.macwire" %% "util" % wireVersion,
      "com.softwaremill.macwire" %% "proxy" % wireVersion,
      "ch.qos.logback" % "logback-core" % "1.4.7",
      "ch.qos.logback" % "logback-classic" % "1.4.7",
//      "org.slf4j" % "slf4j-api" % "2.0.4" % Test,
      // tapir
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-tethys" % tapirVersion,
      // tethys
      "com.tethys-json" %% "tethys-core" % tethysVersion,
      "com.tethys-json" %% "tethys-jackson" % tethysVersion,
      "com.tethys-json" %% "tethys-derivation" % tethysVersion,
      "com.tethys-json" %% "tethys-enumeratum" % tethysVersion,
      // http4s
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      // doobie + quill
      "io.getquill" %% "quill-doobie" % quillVersion,
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari" % doobieVersion,

      // pureconfig
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

      // flyway
      "org.flywaydb" % "flyway-core" % flywayVersion,

    ),
  )
