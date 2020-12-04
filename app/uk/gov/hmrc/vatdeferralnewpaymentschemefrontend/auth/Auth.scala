/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.auth

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.{Configuration, Environment, Mode}
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthProviders, ConfidenceLevel}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{JourneySession, RequestSession, Vrn}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.services.SessionStore
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers.routes
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthImpl])
trait Auth extends AuthorisedFunctions with AuthRedirects with Results {
  def authorise(action: Request[AnyContent] => Vrn => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent]
  def authoriseWithJourneySession(action: Request[AnyContent] => Vrn => JourneySession => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent]
  def authoriseForMatchingVrn(action: Request[AnyContent] => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent]
}

@Singleton
class AuthImpl @Inject()(
  val authConnector: AuthConnector,
  val env: Environment,
  val config: Configuration,
  val appConfig: AppConfig,
  sessionStore: SessionStore) extends Auth {

  def authorise(action: Request[AnyContent] => Vrn => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent] =
    Action.async { implicit request =>

      val currentUrl = {
        if (env.mode.equals(Mode.Dev)) s"http://${request.host}${request.uri}" else s"${request.uri}"
      }

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

      authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L200).retrieve(Retrievals.allEnrolments) {
        enrolments => {

          def getVrnFromEnrolment(serviceKey: String, enrolmentKey: String): Option[Vrn] =
            for {
              e <- enrolments.getEnrolment(serviceKey)
              i <- e.getIdentifier(enrolmentKey)
            } yield Vrn(i.value)

          def getVrnFromSession: Option[Vrn] =
            RequestSession.getObject(request.session).filter(_.isUserEnrolled).map{x => Vrn(x.vrn)}

          (
            getVrnFromEnrolment("HMRC-MTD-VAT", "VRN") orElse
            getVrnFromEnrolment("HMCE-VATDEC-ORG", "VATRegNo") orElse
            getVrnFromSession
          ).fold(Future.successful(Redirect("enter-vrn")))(action(request))
        }
      }.recover {
        case _: NoActiveSession => toGGLogin(currentUrl)
        case _: InsufficientConfidenceLevel | _: InsufficientEnrolments => SeeOther(appConfig.ivUrl(currentUrl))
        case ex => Ok("Error")
      }
    }

  def authoriseWithJourneySession(action: Request[AnyContent] => Vrn => JourneySession => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent] =
    Action.async { implicit request =>

      val currentUrl = {
        if (env.mode.equals(Mode.Dev)) s"http://${request.host}${request.uri}" else s"${request.uri}"
      }

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

      authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L200).retrieve(Retrievals.allEnrolments) {
        enrolments => {

          def getVrnFromEnrolment(serviceKey: String, enrolmentKey: String): Option[Vrn] =
            for {
              e <- enrolments.getEnrolment(serviceKey)
              i <- e.getIdentifier(enrolmentKey)
            } yield Vrn(i.value)

          def getVrnFromSession: Option[Vrn] =
            RequestSession.getObject(request.session).filter(_.isUserEnrolled).map{x => Vrn(x.vrn)}

          def withJourneySession(vrn: Vrn): Future[Result] = request.session.get("sessionId") match {
            case Some(sessionId) =>
              sessionStore.get[JourneySession](sessionId, "JourneySession").flatMap {
                case Some(a) => action(request)(vrn)(a)
                case _ => Future.successful(Redirect(routes.DeferredVatBillController.get()))
              }
            case None => Future.successful(Ok("Session id not set"))
          }

          (
            getVrnFromEnrolment("HMRC-MTD-VAT", "VRN") orElse
            getVrnFromEnrolment("HMCE-VATDEC-ORG", "VATRegNo") orElse
            getVrnFromSession
          ).fold(Future.successful(Redirect("enter-vrn")))(vrn => withJourneySession(vrn))
      }}.recover {
        case _: NoActiveSession => toGGLogin(currentUrl)
        case _: InsufficientConfidenceLevel | _: InsufficientEnrolments => SeeOther(appConfig.ivUrl(currentUrl))
        case ex => Ok(s"Error: $ex")
      }
    }

  def authoriseForMatchingVrn(action: Request[AnyContent] => Future[Result])(implicit ec: ExecutionContext, servicesConfig: ServicesConfig): Action[AnyContent] =
    Action.async { implicit request =>

      val currentUrl = {
        if (env.mode.equals(Mode.Dev)) s"http://${request.host}${request.uri}" else s"${request.uri}"
      }

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, Some(request.session), Some(request))

      authorised(AuthProviders(GovernmentGateway) and ConfidenceLevel.L200).retrieve(Retrievals.allEnrolments) {
        _ => action(request)
      }.recover {
        case _: NoActiveSession => toGGLogin(currentUrl)
        case _: InsufficientConfidenceLevel | _: InsufficientEnrolments => SeeOther(appConfig.ivUrl(currentUrl))
        case ex => Ok("Error")
      }
    }
}