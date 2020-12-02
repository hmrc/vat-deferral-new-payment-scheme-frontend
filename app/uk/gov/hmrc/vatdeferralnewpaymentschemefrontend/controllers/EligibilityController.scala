/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.NotEligiblePage
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import play.api.i18n.I18nSupport

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.VatDeferralNewPaymentSchemeConnector
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{Eligibility, JourneySession}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore

import scala.concurrent.Future

@Singleton
class EligibilityController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector,
  sessionStore: SessionStore,
  notEligiblePage: NotEligiblePage)
  (implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
    extends FrontendController(mcc) with I18nSupport {

  val get: Action[AnyContent] = auth.authorise { implicit request =>
    implicit vrn =>
      vatDeferralNewPaymentSchemeConnector.eligibility(vrn.vrn) map {
        case Eligibility(false, false, true) => {
          request.session.get("sessionId").map(sessionId => {
            sessionStore.store[JourneySession](sessionId, "JourneySession", JourneySession(sessionId, true))
            Redirect(routes.TermsAndConditionsController.get())
          }).getOrElse(Ok("Session id not set"))
        }
        case e => Ok(notEligiblePage(e))
      }
  }
}