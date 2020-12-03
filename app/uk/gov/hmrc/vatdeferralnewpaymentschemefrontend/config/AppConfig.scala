/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config

import java.net.URI
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import java.net.{URLDecoder, URLEncoder}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.models.iv.{ IvResponse, JourneyId }

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  val footerLinkItems: Seq[String] = config.getOptional[Seq[String]]("footerLinkItems").getOrElse(Seq())

  private def getUrlFor(service: String) = servicesConfig.getString(s"microservice.services.$service.url")

  val frontendUrl: String = getUrlFor("frontend")

  val ivUpliftUrl: String = s"${getUrlFor("identity-verification-uplift")}/uplift"

  def ivUrl(redirectOnLoginURL: String): String = {

    def encodedCallbackUrl(redirectOnLoginURL: String): String = URLEncoder.encode(s"$frontendUrl/iv/journey-result?continueURL==$redirectOnLoginURL")

    new URI(
      s"$ivUpliftUrl" +
        s"?origin=VRNPS" +
        s"&completionURL=${encodedCallbackUrl(redirectOnLoginURL)}" +
        s"&failureURL=${encodedCallbackUrl(redirectOnLoginURL)}" +
        "&confidenceLevel=200"
    ).toString
  }

  val ivJourneyResultUrl: String =
    s"${servicesConfig.baseUrl("identity-verification-journey-result")}/mdtp/journey/journeyId"

  def ivJourneyResultUrl(journeyId: JourneyId): String = new URI(s"$ivJourneyResultUrl/${journeyId.Id}").toString

  val enrolmentStoreUrl = s"${servicesConfig.baseUrl("enrolment-store-proxy")}/enrolment-store-proxy/enrolment-store/enrolments"

  val bavfApiBaseUrl = servicesConfig.baseUrl("bank-account-verification-api")
  val bavfWebBaseUrl = servicesConfig.baseUrl("bank-account-verification-web")
}