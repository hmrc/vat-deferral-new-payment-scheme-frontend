/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors

import com.google.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.RootInterface

import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreConnector @Inject()(http: HttpClient)(implicit val appConfig: AppConfig) {

  def checkEnrolments(rootInterface: RootInterface)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url = appConfig.enrolmentStoreUrl
    http.POST[RootInterface, HttpResponse](url, rootInterface)
  }
}