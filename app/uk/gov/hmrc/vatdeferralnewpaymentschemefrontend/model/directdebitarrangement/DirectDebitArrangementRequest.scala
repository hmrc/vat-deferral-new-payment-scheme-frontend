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

case class DirectDebitArrangementRequestAuditWrapper(
  success: Boolean,
  vrn: String,
  arrangement: DirectDebitArrangementRequest
)

object DirectDebitArrangementRequestAuditWrapper {
  implicit val format = Json.format[DirectDebitArrangementRequestAuditWrapper]
}