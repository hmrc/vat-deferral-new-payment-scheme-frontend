import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "vat-deferral-new-payment-scheme-frontend"

PlayKeys.playDefaultPort := 8020

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.11",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig",
      "uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.components._",
      "uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.helpers._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.govukfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._"
    ),
    // ***************
    // Use the silencer plugin to suppress warnings
    // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalacOptions += "-Ypartial-unification")
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427

