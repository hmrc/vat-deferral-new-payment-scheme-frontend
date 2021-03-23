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

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend

import java.time._
import java.time.format.DateTimeFormatter

import play.api.Logger
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.Bavf.InitRequestMessages
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.enrolments.{EnrolmentRequest, EnrolmentResponse, Identifiers, KnownFacts}
import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model.{DateFormValues, MatchingJourneySession}

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.RoundingMode

package object controllers {

  val logger = Logger(this.getClass)

  def formattedPaymentsStartDate(zdt: ZonedDateTime)(implicit messages: Messages) =
    zdt.format(DateTimeFormatter.ofPattern("d MMMM YYYY").withLocale(messages.lang.locale))

  def firstPaymentAmount(amountOwed: BigDecimal, periodOwed: Int): BigDecimal = {
    val monthlyAmount = regularPaymentAmount(amountOwed, periodOwed)
    val remainder = amountOwed - (monthlyAmount * periodOwed)
    monthlyAmount + remainder
  }

  def regularPaymentAmount(amountOwed: BigDecimal, periodOwed: Int): BigDecimal = {
    (amountOwed / periodOwed).setScale(2, RoundingMode.DOWN)
  }

  def daySuffix(day: Int)(implicit messages: Messages): String = {
    if(messages.lang.code == "cy"){
      day.toString
    } else{
      day % 10 match {
        case 1 if day != 11 => "st"
        case 2 if day != 12 => "nd"
        case 3 if day != 13 => "rd"
        case _ => "th"
      }
    }
  }

  def audit[T](
    auditType: String,
    result: T
  )(
    implicit headerCarrier: HeaderCarrier,
    auditConnector: AuditConnector,
    ec: ExecutionContext,
    writes: Writes[T]
  ): Unit = {
    import play.api.libs.json.Json
    auditConnector.sendExplicitAudit(
      auditType,
      Json.toJson(result)(writes)
    )
  }

  def requestMessages(implicit messagesApi: MessagesApi): Option[InitRequestMessages] = {
    val english = messagesApi.preferred(Seq(Lang("en")))
    val welsh = messagesApi.preferred(Seq(Lang("cy")))
    Some(
      InitRequestMessages(
        en = Json.obj(
          "service.name" -> english("service.name"),
          //          TODO: Add in a11y statement after DAC
          //          "footer.accessibility.url" -> s"${appConfig.exampleExternalUrl}${english("footer.accessibility.url")}",
          "phaseBanner.tag" -> "BETA"
        ),
        cy = Some(
          Json.obj(
            "service.name" -> welsh("service.name"),
            //          TODO: Add in a11y statement after DAC
            //          "footer.accessibility.url" -> s"${appConfig.exampleExternalUrl}${welsh("footer.accessibility.url")}",
            "phaseBanner.tag" -> "BETA"
          )
        )
      )
    )
  }

  object enrolments {

    private val HmrcMtdVatService = "HMRC-MTD-VAT"
    private val HmceVatdecOrgService = "HMCE-VATDEC-ORG"

    def enrolmentRequestHmrcMtdVat(matchingJourneySession: MatchingJourneySession) =
      EnrolmentRequest(
        HmrcMtdVatService,
        Seq[KnownFacts](
          KnownFacts("VRN", matchingJourneySession.vrn.getOrElse("")),
          KnownFacts("Postcode", matchingJourneySession.postCode.getOrElse("")))
      )

    def enrolmentRequestHmceVatdecOrg(matchingJourneySession: MatchingJourneySession) =
      EnrolmentRequest(
        HmceVatdecOrgService,
        Seq[KnownFacts](
          KnownFacts("VATRegNo", matchingJourneySession.vrn.getOrElse("")),
          KnownFacts("IRPCODE", matchingJourneySession.postCode.getOrElse("")))
      )

    def enrolmentMatches(
      enrolmentResponse: Option[EnrolmentResponse],
      journeyState: MatchingJourneySession
    ):Boolean = {
      enrolmentResponse match {
        case Some(er) if er.enrolments.isEmpty => {
          logger.warn("VDNPS: Enrolment empty: true")
          false
        }
        case Some(er) =>
          logger.warn("VDNPS: Enrolment empty: false")
          er.enrolments.forall(
            enrolment => enrolment.verifiers.forall({
              logger.logger.warn(s"VDNPS: Enrolment response: verifiers: ${enrolment.verifiers.map(x => x.key)}")
              identifiers =>
                checkEnrolments(er.service, identifiers, journeyState)
            })
          )
        case _ => {
          logger.warn("VDNPS: Enrolment: None")
          false
        }
      }
    }

//    private def checkVatRegistrationDate(
//      identifierValue: String,
//      stateValue: Option[DateFormValues]
//    ): Boolean = {
//      identifierValue == formatStateDate(stateValue).getOrElse("")
//    }

    private def checkVatRegistrationDate(
      identifierValue: String,
      stateValue: Option[DateFormValues]
    ): Boolean = {
      val formatter = format.DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd/MM/yy][ddMMyy]")
      val parseDate = LocalDate.parse(identifierValue, formatter)

      parseDate.isEqual(formatStateDate(stateValue).getOrElse(LocalDate.of(1900,1,1)))
    }

//    private def formatStateDate(date: Option[DateFormValues]): Option[String] =
//      date.map(dt => s"${"%02d".format(dt.day.toInt)}/${"%02d".format(dt.month.toInt)}/${dt.year.takeRight(2)}")

    private def formatStateDate(date: Option[DateFormValues]): Option[LocalDate] =
      date.map(dt => LocalDate.of(dt.year.toInt, dt.month.toInt, dt.day.toInt))


    private def formatLastAccountPeriodMonth(month: Option[String]) =
      month.fold("") { x =>
        LocalDate.now.withMonth(
          Integer.parseInt(x)
        ).format(
          DateTimeFormatter.ofPattern("MMM")
        ).toLowerCase()
      }

    private def checkEnrolments(
      service: String,
      identifiers: Identifiers,
      mjs: MatchingJourneySession
    ): Boolean = (service, identifiers.key, identifiers.value) match {
      case (HmrcMtdVatService, "BoxFiveValue", v) => {
        logger.warn(s"VDNPS: BoxFiveValue: ${v == mjs.latestVatAmount.getOrElse("")}")
        v == mjs.latestVatAmount.getOrElse("")
      }
      case (HmrcMtdVatService, "LastMonthLatestStagger", v) => {
        logger.warn(s"VDNPS: LastMonthLatestStagger: ${v == mjs.latestAccountPeriodMonth.getOrElse("")}")
        v == mjs.latestAccountPeriodMonth.getOrElse("")
      }
      case (HmrcMtdVatService, "VATRegistrationDate", v) => {
        logger.warn(s"VDNPS: VATRegistrationDate: ${checkVatRegistrationDate(v, mjs.date)}, length: ${v.length}, format: ${v.replaceAll("[0-9]", "x")}")
        checkVatRegistrationDate(v, mjs.date)
      }
//      case (HmceVatdecOrgService, "PETAXDUESALES", v) =>  {
//        logger.warn(s"VDNPS: PETAXDUESALES: ${v == mjs.latestVatAmount.getOrElse("") }")
//        v == mjs.latestVatAmount.getOrElse("")
//      }
      case (HmceVatdecOrgService, "PEPDNO", v) => {
        logger.warn(s"VDNPS: PEPDNO: ${v.toLowerCase() == formatLastAccountPeriodMonth(mjs.latestAccountPeriodMonth)}")
        v.toLowerCase() == formatLastAccountPeriodMonth(mjs.latestAccountPeriodMonth)
      }
      case (HmceVatdecOrgService, "IREFFREGDATE", v) =>
        logger.warn(s"VDNPS: IREFFREGDATE: ${checkVatRegistrationDate(v, mjs.date) }, length: ${v.length}, format: ${v.replaceAll("[0-9]", "x")}")
        checkVatRegistrationDate(v, mjs.date)
      case _ => true
    }
  }
}
