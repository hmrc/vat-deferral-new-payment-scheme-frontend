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
import play.api.libs.json.Writes
import play.api.mvc._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors.VatDeferralNewPaymentSchemeConnector
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{Eligibility, JourneySession}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.ReturningUserPage
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.errors._

import scala.concurrent.ExecutionContext

@Singleton
class EligibilityController @Inject()(
  auth: Auth,
  vatDeferralNewPaymentSchemeConnector: VatDeferralNewPaymentSchemeConnector,
  sessionStore: SessionStore,
  notEligiblePage: NotEligiblePage,
  returningUserPage: ReturningUserPage,
  noDeferredVatToPayPage: NoDeferredVatToPayPage,
  timeToPayExistsPage: TimeToPayExistsPage,
  paymentOnAccountExistsPage: PaymentOnAccountExistsPage,
  outstandingReturnsPage: OutstandingReturnsPage
)(
  implicit val appConfig: AppConfig,
  mcc: MessagesControllerComponents,
  val serviceConfig: ServicesConfig,
  ec: ExecutionContext,
  auditConnector: AuditConnector
) extends FrontendController(mcc)
  with I18nSupport {

  val logger = Logger(getClass)

  def get: Action[AnyContent] = auth.authorise { implicit request =>
    implicit vrn => {

      implicit val auditWrites: Writes[Eligibility] = Eligibility.auditWrites

      for {
        e <- vatDeferralNewPaymentSchemeConnector.eligibility(vrn.vrn)
        _ = audit("EligibilityCheck", e)
      } yield {
        e match {
        case e:Eligibility if e.eligible =>
          request.session.get("sessionId").map { sessionId =>
            sessionStore.store[JourneySession](sessionId, "JourneySession", JourneySession(sessionId, eligible = true))
            Redirect(routes.CheckBeforeYouStartController.get())
          }.getOrElse {
            logger.warn("Unable to retirieve sessionId from request")
            InternalServerError
          }
        case Eligibility(Some(true),_,_,_,_) =>
          Ok(returningUserPage())
        case Eligibility(_,Some(true),_,_,_) =>
          Ok(paymentOnAccountExistsPage())
        case Eligibility(_,_,Some(true),_,_) =>
          Ok(timeToPayExistsPage())
        case Eligibility(_,_,_,Some(true),_) =>
          Ok(outstandingReturnsPage())
        case Eligibility(_,_,_,_,None) =>
          Ok(noDeferredVatToPayPage())
        case _ =>
          Ok(notEligiblePage()) // TODO - this page will never be shown and can be removed
      }}
    }
  }
}