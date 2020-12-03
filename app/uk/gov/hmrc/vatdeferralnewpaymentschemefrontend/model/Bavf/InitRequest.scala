/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class InitRequest(serviceIdentifier: String,
                       continueUrl: String,
                       messages: Option[InitRequestMessages] = None,
                       customisationsUrl: Option[String] = None,
                       address: Option[InitRequestAddress] = None)

object InitRequest {
  implicit val messagesWrites: OWrites[InitRequestMessages] = Json.writes[InitRequestMessages]
  implicit val addressWrites: OWrites[InitRequestAddress] = Json.writes[InitRequestAddress]
  implicit val writes: Writes[InitRequest] = Json.writes[InitRequest]
}