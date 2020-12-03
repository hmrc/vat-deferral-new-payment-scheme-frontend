/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

case class InitRequestAddress(lines: List[String], town: Option[String], postcode: Option[String])
