lazy val root = project
  .in(file("."))
  .aggregate(core)
  .settings(noPublishSettings)
  .settings(commonSettings, releaseSettings)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings, releaseSettings)
  .settings(name := "epitest-cats-scalacheck")

lazy val docs = project
  .in(file("docs"))
  .settings(noPublishSettings)
  .settings(commonSettings, micrositeSettings)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(TutPlugin)
  .dependsOn(core)

val catsV = "1.6.0"
val scalacheckV = "1.13.5"

lazy val contributors = Seq(
  "ChristopherDavenport" -> "Christopher Davenport",
  "Epitech" -> "Epitech"
)

lazy val commonSettings = Seq(
  organization := "eu.epitech",
  scalaVersion := "2.12.8",
  addCompilerPlugin("org.spire-math" % "kind-projector"      % "0.9.9" cross CrossVersion.binary),
  addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.0-M4"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"    % catsV,
    "org.scalacheck" %% "scalacheck"  % scalacheckV,
    "org.typelevel" %% "cats-laws"    % catsV % Test,
    "org.typelevel" %% "cats-testkit" % catsV % Test
  )
)

lazy val releaseSettings = {
  Seq(
    publishTo := {
      val nexus = "https://nexus.epitest.eu/repository/maven-"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "snapshots")
      else
        Some("releases" at nexus + "releases")
    },
    publishArtifact in Test := false,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/Epitech/epitest-cats-scalacheck"),
        "git@github.com:Epitech/epitest-cats-scalacheck.git"
      )
    ),
    homepage := Some(url("https://github.com/Epitech/cats-scalacheck")),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    publishMavenStyle := true,
    pomIncludeRepository := { _ =>
      false
    },
    pomExtra := {
      <developers>
        {for ((username, name) <- contributors) yield
        <developer>
          <id>{username}</id>
          <name>{name}</name>
          <url>http://github.com/{username}</url>
        </developer>
        }
      </developers>
    }
  )
}

lazy val micrositeSettings = Seq(
  micrositeName := "cats-scalacheck",
  micrositeDescription := "Cats Instances for Scalacheck",
  micrositeAuthor := "Christopher Davenport",
  micrositeGithubOwner := "ChristopherDavenport",
  micrositeGithubRepo := "cats-scalacheck",
  micrositeBaseUrl := "/cats-scalacheck",
  micrositeDocumentationUrl := "https://christopherdavenport.github.io/cats-scalacheck",
  micrositeFooterText := None,
  micrositeHighlightTheme := "atom-one-light",
  micrositePalette := Map(
    "brand-primary" -> "#3e5b95",
    "brand-secondary" -> "#294066",
    "brand-tertiary" -> "#2d5799",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  fork in tut := true,
  scalacOptions in Tut --= Seq(
    "-Xfatal-warnings",
    "-Ywarn-unused-import",
    "-Ywarn-numeric-widen",
    "-Ywarn-dead-code",
    "-Ywarn-unused:imports",
    "-Xlint:-missing-interpolator,_"
  )
)

lazy val noPublishSettings = {
  Seq(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )
}
