/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait Account

case class BusinessCompleteResponse(companyName: String, sortCode: String, accountNumber: String) extends Account

case class PersonalCompleteResponse(accountName: String, sortCode: String, accountNumber: String) extends Account

object Account {

  implicit val businessReads: Reads[BusinessCompleteResponse] = (
      (__ \ "business" \ "companyName").read[String] and
      (__ \ "business" \ "sortCode").read[String] and
      (__ \ "business" \ "accountNumber").read[String]
    )(BusinessCompleteResponse.apply _)

  implicit val personalReads: Reads[PersonalCompleteResponse] = (
    (__ \ "personal" \ "accountName").read[String] and
    (__ \ "personal" \ "sortCode").read[String] and
    (__ \ "personal" \ "accountNumber").read[String]
    )(PersonalCompleteResponse.apply _)

  implicit val accountReads: Reads[Account] = Reads[Account](jsValue => {
    (jsValue \ "accountType").toEither
      .map(
        v =>
          v.as[String] match {
            case "business" => businessReads.map(_.asInstanceOf[Account]).reads(jsValue)
            case "personal" => personalReads.map(_.asInstanceOf[Account]).reads(jsValue)
            case t =>
              JsError(__ \ "accountType", s"unknown account type $t")
                .asInstanceOf[JsResult[Account]]
          }
      )
      .fold(v => JsError(JsPath, v).asInstanceOf[JsResult[Account]], v => v)
  })
}