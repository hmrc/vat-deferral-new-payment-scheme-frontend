/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class InitResponse(journeyId: String, startUrl: String, completeUrl: String, detailsUrl: Option[String])

object InitResponse {
  implicit def writes: OWrites[InitResponse] = Json.writes[InitResponse]
  implicit def reads: Reads[InitResponse] = Json.reads[InitResponse]
}