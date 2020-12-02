/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.HowManyMonthsPage
import scala.concurrent.Future
import play.api.i18n.I18nSupport
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.viewmodel.Month
import scala.math.BigDecimal.RoundingMode
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.JourneySession

@Singleton
class MonthsController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  howManyMonthsPage: HowManyMonthsPage,
  sessionStore: SessionStore)
  (implicit val appConfig: AppConfig, val serviceConfig: ServicesConfig)
    extends FrontendController(mcc) with I18nSupport {

  val get: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

    journeySession.outStandingAmount match {
      case Some(outStandingAmount) => Future.successful(Ok(howManyMonthsPage(getMonths(outStandingAmount))))
      case _ => Future.successful(Redirect(routes.DeferredVatBillController.get()))
    }
  }

  val post: Action[AnyContent] = auth.authoriseWithJourneySession { implicit request => vrn => journeySession =>

      val months = request.body.asFormUrlEncoded.map(a => a.mapValues(_.last)).flatMap(b => b.get("how-many-months"))

      months.fold(Future.successful(BadRequest("error occured")))(month => {
        sessionStore.store[JourneySession](journeySession.id, "JourneySession", journeySession.copy(numberOfPaymentMonths = Some(month.toInt)))
        Future.successful(Redirect(routes.WhenToPayController.get()))
      })
  }

  def getMonths(amount: BigDecimal): Seq[Month] = {
    (2 to 11).map {
      month => {
        val monthlyAmount = (amount / month).setScale(2, RoundingMode.DOWN)
        val remainder = amount - (monthlyAmount * month)
        Month(month.toString, monthlyAmount.toString, remainder.toString)
      }
    }
  }
}
