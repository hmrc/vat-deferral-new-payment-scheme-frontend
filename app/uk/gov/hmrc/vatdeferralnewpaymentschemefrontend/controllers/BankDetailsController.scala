/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.{BavfConnector, VatDeferralNewPaymentSchemeConnector}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.directdebitarrangement.DirectDebitArrangementRequest
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.DirectDebitPage

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class BankDetailsController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  directDebitPage: DirectDebitPage,
  connector: BavfConnector,
  sessionStore: SessionStore,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector)
  (implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
  extends FrontendController(mcc) with I18nSupport {

  def get(journeyId: String): Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>
      connector.complete(journeyId).map {
        case Some(r) => Ok(directDebitPage(journeyId))
        case None => InternalServerError
      }
  }

  def post(journeyId: String): Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

    def submitDirectDebitArrangement(sortCode: String, accountNumber: String, accountName: String) = {
      val directDebitArrangementRequest = DirectDebitArrangementRequest(
        journeySession.dayOfPayment.getOrElse(throw new RuntimeException(s"Day of payment is not set")),
        journeySession.numberOfPaymentMonths.getOrElse(throw new RuntimeException(s"Number of payment months not set")),
        journeySession.outStandingAmount.getOrElse(throw new RuntimeException(s"Outstanding amount is not set")),
        sortCode,
        accountNumber,
        accountName
      )
      vatDeferralNewPaymentSchemeConnector.createDirectDebitArrangement(vrn.vrn, directDebitArrangementRequest)
      Redirect(routes.ConfirmationController.get())
    }

    connector.complete(journeyId).map {
      case Some(r) if r.accountType == "personal" => submitDirectDebitArrangement(r.personal.get.sortCode, r.personal.get.accountNumber, r.personal.get.accountName)
      case Some(r) if r.accountType == "business" => submitDirectDebitArrangement(r.business.get.sortCode, r.business.get.accountNumber, r.business.get.companyName)
      case None => InternalServerError
    }
  }
}