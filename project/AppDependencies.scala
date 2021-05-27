import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-27"       % "4.0.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"               % "0.38.0-play-27",
    "uk.gov.hmrc"             %% "play-frontend-govuk"              % "0.60.0-play-27",
    "uk.gov.hmrc"             %% "mongo-caching"                    % "6.16.0-play-27",
    "uk.gov.hmrc"             %% "digital-engagement-platform-chat" % "0.15.0-play-27",
    "uk.gov.hmrc"             %% "play-language"                    % "4.10.0-play-27",
    "com.chuusai"             %% "shapeless"                        % "2.3.3",
    "org.typelevel"           %% "cats-core"                        % "2.4.2"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "3.0.0" % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.2"                 % Test,
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % Test,
    "com.typesafe.play"       %% "play-test"                % current                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"               % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"                 % "test, it",
    "org.scalatestplus"       %% "mockito-3-4"              % "3.2.2.0"               % "test, it"

  )
}
