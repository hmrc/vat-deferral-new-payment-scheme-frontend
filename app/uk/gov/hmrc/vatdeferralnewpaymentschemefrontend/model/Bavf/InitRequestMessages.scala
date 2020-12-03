/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf

import play.api.libs.json.JsObject

case class InitRequestMessages(en: JsObject, cy: Option[JsObject] = None)
