/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors

import javax.inject.Inject
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf._
import HttpReads.Implicits.readRaw
import InitRequest.writes

import scala.concurrent.{ExecutionContext, Future}

class BavfConnector @Inject()(httpClient: HttpClient)(implicit val appConfig: AppConfig) {

  def init(continueUrl: String,
           messages: Option[InitRequestMessages] = None,
           customisationsUrl: Option[String] = None)(
            implicit ec: ExecutionContext,
            hc: HeaderCarrier
          ): Future[Option[InitResponse]] = {

    val request = InitRequest(
      "vdnps",
      continueUrl,
      messages,
      customisationsUrl,
      address = None)

    val url = s"${appConfig.bavfApiBaseUrl}/api/init"
    httpClient.POST[InitRequest, HttpResponse](url, request).map {
      case r if r.status == 200 =>
        Some(r.json.as[InitResponse])
      case _ =>
        None
    }
  }

  def complete(journeyId: String)(
    implicit ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Option[CompleteResponse]] = {
    import CompleteResponse._
    import HttpReads.Implicits.readRaw

    val url = s"${appConfig.bavfApiBaseUrl}/api/complete/$journeyId"
    httpClient.GET[HttpResponse](url).map {
      case r if r.status == 200 =>
        Some(r.json.as[CompleteResponse])
      case _ =>
        None
    }
  }
}


