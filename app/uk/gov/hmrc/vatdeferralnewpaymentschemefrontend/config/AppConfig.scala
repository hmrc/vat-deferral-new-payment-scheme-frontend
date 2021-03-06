/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config

import java.net.{URI, URLEncoder}
import java.time.LocalDate

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers.routes
import play.api.i18n.Lang

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {

  private def getUrlFor(service: String) = servicesConfig.getString(s"microservice.services.$service.url")

  lazy val appName: String = config.get[String]("appName")

  val frontendUrl: String = getUrlFor("frontend")

  val ggContinueUrlPrefix: String =
    servicesConfig.getString("microservice.services.bas-gateway-frontend.continue-url-prefix")

  val ggUserUrl: String =
    s"${getUrlFor("government-gateway-registration")}/government-gateway-registration-frontend?" +
      "accountType=individual&" +
      s"continue=${URLEncoder.encode(ggContinueUrlPrefix, "UTF-8")}%2Fvat-deferral-new-payment-scheme%2Feligibility&" +
      "origin=vat-deferral-new-payment-scheme-frontend&" +
      "registerForSa=skip"
  lazy val sessionTimeout = config.get[String]("application.session.maxAge")

  val enrolmentStoreUrl = s"${servicesConfig.baseUrl("enrolment-store-proxy")}/enrolment-store-proxy/enrolment-store/enrolments"

  lazy val feedbackSurveyUrl: String = servicesConfig.getConfString("feedback-survey.url", "")

  val contactHost: String = servicesConfig.baseUrl(s"contact-frontend")
  //TODO Check the service identifier for feedback - get Shep to check, AD
  lazy val betaFeedbackUrlNoAuth = s"$contactHost/contact/beta-feedback-unauthenticated?service=VDNPS"

  private lazy val basGatewayFrontend = servicesConfig.getConfString("bas-gateway-frontend.url", "")
  private lazy val basGatewaySignInPath = servicesConfig.getConfString("bas-gateway-frontend.sign-in-path", "")
  private lazy val basGatewaySignOutPath = servicesConfig.getConfString("bas-gateway-frontend.sign-out-path", "")

  lazy val signInUrl: String = s"$basGatewayFrontend$basGatewaySignInPath"
  lazy val signOutUrl: String = s"$basGatewayFrontend$basGatewaySignOutPath?continue=$feedbackSurveyUrl"

  val bavfApiBaseUrl = servicesConfig.baseUrl("bank-account-verification-api")
  val bavfWebBaseUrl = servicesConfig.baseUrl("bank-account-verification-web")

  val vrnRegex = servicesConfig.getString(s"regex.vrn")
  val moneyWithCommaRegex = servicesConfig.getString(s"regex.moneyWithComma")
  val postCodeRegex = servicesConfig.getString(s"regex.postCode")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy"))

  def routeToSwitchLanguage = (lang: String) => routes.CustomLanguageController.switchToLanguage(lang)

  lazy val languageTranslationEnabled: Boolean =
    config.getOptional[Boolean]("microservice.services.features.welsh-translation").getOrElse(false)

}