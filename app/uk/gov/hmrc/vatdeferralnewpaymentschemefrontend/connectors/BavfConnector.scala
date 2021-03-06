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

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors

import com.google.inject.ImplementedBy
import javax.inject.Inject
import uk.gov.hmrc.http._
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf._
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers.audit
import play.api.Logger
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[BavfConnectorImpl])
trait BavfConnector {

  val logger = Logger(getClass)

  def init(
    continueUrl: String,
    messages: Option[InitRequestMessages] = None,
    customisationsUrl: Option[String] = None,
    prepopulatedData: Option[InitRequestPrepopulatedData] = None
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InitResponse]

  def complete(journeyId: String, vrn: String)(
    implicit ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Account]
}

class BavfConnectorImpl @Inject()(
  httpClient: HttpClient
)(
  implicit val appConfig: AppConfig,
  auditConnector: AuditConnector
) extends BavfConnector {


  def init(
    continueUrl: String,
    messages: Option[InitRequestMessages] = None,
    customisationsUrl: Option[String] = None,
    prepopulatedData: Option[InitRequestPrepopulatedData] = None
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[InitResponse] = {

    val request = InitRequest(
      "vdnps",
      continueUrl,
      prepopulatedData,
      messages,
      customisationsUrl
    )

    val url = s"${appConfig.bavfApiBaseUrl}/api/init"
    httpClient.POST[InitRequest, InitResponse](url, request).recover {
      case e: UpstreamErrorResponse =>
        logger.warn(s"init connector failed to BAVFE with statusCode: ${e.statusCode} and message: ${e.message}")
        throw e
    }
  }

  def complete(journeyId: String, vrn: String)(
    implicit ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Account] = {
    val url = s"${appConfig.bavfApiBaseUrl}/api/complete/$journeyId"
    httpClient.GET[Account](url).recover {
      case e: UpstreamErrorResponse =>
        audit[AccountVerificationAuditWrapper](
          "BankAccountVerificationStride",
          AccountVerificationAuditWrapper(verified = false, vrn, None)
        )
        logger.warn(s"JourneyId: $journeyId - complete connector failed from BAVFE with statusCode: ${e.statusCode} and message: ${e.message}")
      throw e
    }
  }
}


