/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class CompleteResponse(accountType: String,
                            personal: Option[PersonalCompleteResponse],
                            business: Option[BusinessCompleteResponse])

object CompleteResponse {
  implicit val addressReads: Reads[CompleteResponseAddress] = Json.reads[CompleteResponseAddress]
  implicit val reads: Reads[CompleteResponse] = Json.reads[CompleteResponse]
}