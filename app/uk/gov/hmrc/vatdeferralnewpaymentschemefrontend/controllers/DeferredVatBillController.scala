/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.DeferredVatBillPage
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{FinancialData, JourneySession}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.VatDeferralNewPaymentSchemeConnector
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore

import scala.concurrent.Future

@Singleton
class DeferredVatBillController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector,
  deferredVatBillPage: DeferredVatBillPage,
  sessionStore: SessionStore)
  (implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
    extends FrontendController(mcc) with I18nSupport {

  def get(): Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>
      vatDeferralNewPaymentSchemeConnector.financialData(vrn.vrn) map { e =>
        sessionStore.store[JourneySession](journeySession.id, "JourneySession", journeySession.copy(outStandingAmount = Some(e.outstandingAmount)))
        Ok(deferredVatBillPage(e.originalAmount, e.outstandingAmount, e.originalAmount - e.outstandingAmount))
      }
  }
}
