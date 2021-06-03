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

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.VatDeferralNewPaymentSchemeConnector
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.ConfirmationPage
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.components.PaymentSummary

import scala.concurrent.ExecutionContext

@Singleton
class ConfirmationController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  confirmationPage: ConfirmationPage,
  paymentSummary: PaymentSummary,
  sessionStore: SessionStore,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector
)(
  implicit val appConfig: AppConfig,
  val serviceConfig: ServicesConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
  with I18nSupport {

  def get(): Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

    val oa :BigDecimal = journeySession.outStandingAmount.getOrElse(0)

    val fpa: BigDecimal = firstPaymentAmount(
      oa,
      journeySession.numberOfPaymentMonths.getOrElse(11)
    )

    vatDeferralNewPaymentSchemeConnector.firstPaymentDate(vrn).map { paymentStartDate =>
      val dop = journeySession.dayOfPayment.fold(throw new IllegalStateException("Missing recurring monthly payment day")){
        dop => paymentStartDate.plusMonths(1L).withDayOfMonth(dop)
      }
      val rpa = regularPaymentAmount(oa, journeySession.numberOfPaymentMonths.getOrElse(11))
      val ps = paymentSummary(
        paymentStartDate,
        journeySession.dayOfPayment.getOrElse(0),
        journeySession.numberOfPaymentMonths.getOrElse(11),
        journeySession.outStandingAmount.getOrElse(BigDecimal(0)),
        firstPaymentAmount(oa, journeySession.numberOfPaymentMonths.getOrElse(11)),
        rpa
      )
      request.session.get("sessionId").fold(())(sessionStore.drop)
      Ok(confirmationPage(paymentStartDate,fpa,dop,rpa,ps)).withNewSession
    }
  }
}