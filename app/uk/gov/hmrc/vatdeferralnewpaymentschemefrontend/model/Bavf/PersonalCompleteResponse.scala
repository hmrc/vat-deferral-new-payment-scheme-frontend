/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class PersonalCompleteResponse(address: Option[CompleteResponseAddress],
                                    accountName: String,
                                    sortCode: String,
                                    accountNumber: String,
                                    accountNumberWithSortCodeIsValid: ReputationResponseEnum,
                                    rollNumber: Option[String],
                                    accountExists: Option[ReputationResponseEnum],
                                    nameMatches: Option[ReputationResponseEnum],
                                    addressMatches: Option[ReputationResponseEnum],
                                    nonConsented: Option[ReputationResponseEnum],
                                    subjectHasDeceased: Option[ReputationResponseEnum],
                                    nonStandardAccountDetailsRequiredForBacs: Option[ReputationResponseEnum],
                                    sortCodeBankName: Option[String])

object PersonalCompleteResponse {
  implicit val addressReads: Reads[CompleteResponseAddress] = Json.reads[CompleteResponseAddress]
  implicit val reads: Reads[PersonalCompleteResponse] = Json.reads[PersonalCompleteResponse]
}