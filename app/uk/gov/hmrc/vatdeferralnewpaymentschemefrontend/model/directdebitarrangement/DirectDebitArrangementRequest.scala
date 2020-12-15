/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.directdebitarrangement

import play.api.libs.json.Json

case class DirectDebitArrangementRequest(
  paymentDay: Int,
  numberOfPayments: Int,
  totalAmountToPay: BigDecimal,
  sortCode: String,
  accountNumber: String,
  accountName: String)

object DirectDebitArrangementRequest {
  implicit val format = Json.format[DirectDebitArrangementRequest]
}