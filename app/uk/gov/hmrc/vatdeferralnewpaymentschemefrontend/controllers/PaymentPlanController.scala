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
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.{BavfConnector, VatDeferralNewPaymentSchemeConnector}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.PaymentPlanPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentPlanController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  paymentPlanPage: PaymentPlanPage,
  connector: BavfConnector,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector
)(
  implicit val appConfig: AppConfig,
  val serviceConfig: ServicesConfig,
  ec: ExecutionContext
) extends FrontendController(mcc)
  with I18nSupport {

  val logger = Logger(getClass)

  def get: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

    (journeySession.dayOfPayment, journeySession.outStandingAmount) match {
      case (Some(dayOfPayment), Some(outStandingAmount)) =>
        vatDeferralNewPaymentSchemeConnector.firstPaymentDate(vrn).map { paymentStartDate =>
          Ok(
            paymentPlanPage(
              paymentStartDate,
              dayOfPayment,
              journeySession.numberOfPaymentMonths.getOrElse(11),
              outStandingAmount,
              firstPaymentAmount(outStandingAmount, journeySession.numberOfPaymentMonths.getOrElse(11)),
              regularPaymentAmount(outStandingAmount, journeySession.numberOfPaymentMonths.getOrElse(11))
            )
          )
        }
      case _ =>
        logger.warn("dayOfPayment and outStandingAmount cannot be retrieved from journeySession")
        Future.successful(Redirect(routes.DeferredVatBillController.get()))
    }
  }

  def post: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request =>
    _ =>
      _ =>
        val continueUrl = s"${appConfig.frontendUrl}/check-the-account-details"
        connector.init(continueUrl, requestMessages).map { initResponse =>
          SeeOther(s"${appConfig.bavfWebBaseUrl}${initResponse.startUrl}")
        }
  }
}
