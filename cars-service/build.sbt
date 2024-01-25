import com.typesafe.sbt.packager.docker.{DockerChmodType, DockerPermissionStrategy}

name := """cars-service"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.12"

libraryDependencies += guice
libraryDependencies += "org.playframework" %% "play-slick" % "6.0.0"
libraryDependencies += "com.h2database" % "h2" % "2.2.224"
libraryDependencies += "org.postgresql" % "postgresql" % "42.5.4"
libraryDependencies += specs2 % Test
libraryDependencies += "org.specs2" %% "specs2-matcher-extra" % "4.19.2" % Test

Test / javaOptions ++= Seq("-Dconfig.file=conf/test.conf")

Docker / maintainer := "yarutin.eu@gmail.com"
Docker / packageName := "cars-service"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "0")
dockerExposedPorts := Seq(9000)
dockerBaseImage := "openjdk:11"
dockerRepository := sys.env.get("ecr_repo")
dockerUpdateLatest := true
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
