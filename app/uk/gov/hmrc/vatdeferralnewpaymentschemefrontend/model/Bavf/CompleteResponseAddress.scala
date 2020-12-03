/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json._

case class CompleteResponseAddress(lines: List[String], town: Option[String], postcode: Option[String]) {
  override def toString: String = {
    (lines ++ Seq(town, postcode).flatten).mkString("<br>")
  }
}
