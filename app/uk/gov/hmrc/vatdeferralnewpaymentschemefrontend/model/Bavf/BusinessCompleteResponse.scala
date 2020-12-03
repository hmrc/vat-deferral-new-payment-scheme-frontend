/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class BusinessCompleteResponse(address: Option[CompleteResponseAddress],
                                    companyName: String,
                                    sortCode: String,
                                    accountNumber: String,
                                    rollNumber: Option[String],
                                    accountNumberWithSortCodeIsValid: ReputationResponseEnum,
                                    accountExists: Option[ReputationResponseEnum],
                                    companyNameMatches: Option[ReputationResponseEnum],
                                    companyPostCodeMatches: Option[ReputationResponseEnum],
                                    companyRegistrationNumberMatches: Option[ReputationResponseEnum],
                                    nonStandardAccountDetailsRequiredForBacs: Option[ReputationResponseEnum],
                                    sortCodeBankName: Option[String])

object BusinessCompleteResponse {
  implicit val addressReads: Reads[CompleteResponseAddress] = Json.reads[CompleteResponseAddress]
  implicit val completeResponseReads: Reads[BusinessCompleteResponse] = Json.reads[BusinessCompleteResponse]
}