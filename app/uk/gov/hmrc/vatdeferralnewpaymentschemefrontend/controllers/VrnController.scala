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
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth.Auth
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{MatchingJourneySession, Vrn}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.views.html.EnterVrnPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VrnController @Inject()(
  mcc: MessagesControllerComponents,
  auth: Auth,
  sessionStore: SessionStore,
  enterVrnPage: EnterVrnPage
)(
  implicit val appConfig: AppConfig,
  val serviceConfig: ServicesConfig,
  ec: ExecutionContext
) extends BaseController(mcc) {

  def get(): Action[AnyContent] = auth.authoriseWithMatchingJourneySession { implicit request => matchingJourneySession =>
    Future.successful(Ok(
      enterVrnPage(
        matchingJourneySession.vrn.fold(frm){ x =>
          frm.fill(Vrn(x))
        }
      )
    ))
  }

  def post(): Action[AnyContent] = auth.authoriseWithMatchingJourneySession { implicit request => matchingJourneySession =>

    frm.bindFromRequest().fold(
        errors => Future(BadRequest(enterVrnPage(errors))),
        vrn => {
          sessionStore.store[MatchingJourneySession](matchingJourneySession.id, "MatchingJourneySession", matchingJourneySession.copy(vrn = Some(vrn.vrn)))
          Future.successful(Redirect(routes.PostCodeController.get()))
        }
      )
  }

  val frm: Form[Vrn] = Form(mapping("vrn" -> mandatoryAndValid("vrn", appConfig.vrnRegex))(Vrn.apply)(Vrn.unapply))
}