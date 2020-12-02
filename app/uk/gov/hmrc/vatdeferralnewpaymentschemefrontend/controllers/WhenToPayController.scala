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
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.JourneySession
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.WhenToPayPage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class WhenToPayController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  whenToPagePage: WhenToPayPage,
  sessionStore: SessionStore)
    (implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
    extends FrontendController(mcc) with I18nSupport {

  val get: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

    journeySession.outStandingAmount match {
      case Some(_) => Future.successful(Ok(whenToPagePage()))
      case _ => Future.successful(Redirect(routes.DeferredVatBillController.get()))
    }
  }

  val post: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

      val days = request.body.asFormUrlEncoded.map(a => a.mapValues(_.last)).flatMap(b => b.get("alt-day"))

      days.fold(Future.successful(BadRequest("error occured")))(day => {
        sessionStore.store[JourneySession](journeySession.id, "JourneySession", journeySession.copy(dayOfPayment = Some(if (day.isEmpty) 28 else day.toInt ) ))
        Future.successful(Redirect(""))
      })
  }
}
