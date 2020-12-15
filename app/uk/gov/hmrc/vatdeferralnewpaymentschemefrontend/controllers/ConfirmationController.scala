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
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.ConfirmationPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ConfirmationController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  confirmationPage: ConfirmationPage)(implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
  extends FrontendController(mcc) with I18nSupport {

  def get(): Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>
      Future.successful(Ok(confirmationPage()))
  }
}