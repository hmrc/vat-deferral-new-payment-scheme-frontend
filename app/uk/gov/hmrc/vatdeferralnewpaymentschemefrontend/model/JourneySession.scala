/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model

import play.api.libs.json.Json
import play.api.mvc

case class JourneySession (id: String, eligible: Boolean = false, outStandingAmount: Option[BigDecimal] = None, numberOfPaymentMonths: Option[Int] = None, dayOfPayment: Option[Int] = None)

object JourneySession {
  implicit val formats = Json.format[JourneySession]
}