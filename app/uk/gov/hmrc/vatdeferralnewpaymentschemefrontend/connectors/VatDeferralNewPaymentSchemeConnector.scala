/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.connectors

import com.google.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.config.AppConfig
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.directdebitarrangement.DirectDebitArrangementRequest
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{Eligibility, FinancialData}

import scala.concurrent.ExecutionContext

class VatDeferralNewPaymentSchemeConnector @Inject()(http: HttpClient, servicesConfig: ServicesConfig)(implicit val appConfig: AppConfig) {

  lazy val serviceURL = servicesConfig.baseUrl("vat-deferral-new-payment-scheme-service")

  def eligibility(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext)= {
    val url = s"${serviceURL}/vat-deferral-new-payment-scheme/eligibility/$vrn"
    http.GET[Eligibility](url)
  }

  def financialData(vrn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext)= {
    val url = s"${serviceURL}/vat-deferral-new-payment-scheme/financialData/$vrn"
    http.GET[FinancialData](url)
  }

  def createDirectDebitArrangement(vrn: String, directDebitArrangementRequest: DirectDebitArrangementRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)= {
    val url = s"${serviceURL}/vat-deferral-new-payment-scheme/direct-debit-arrangement/$vrn"
    http.POST[DirectDebitArrangementRequest, HttpResponse](url, directDebitArrangementRequest)
  }
}