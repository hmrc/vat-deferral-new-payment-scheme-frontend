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

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.model

import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

case class DateFormValues(day: String, month: String, year: String)

case object DateFormValues {
  implicit val format = Json.format[DateFormValues]
}

case class MatchingJourneySession (
  id: String,
  vrn: Option[String] = None,
  postCode: Option[String] = None,
  latestVatAmount: Option[String] = None,
  latestAccountPeriodMonth: Option[String] = None,
  date: Option[DateFormValues] = None,
  isUserEnrolled: Boolean = false,
  failedMatchingAttempts: Int = 0
) {
  // n.b. the session collection has a ttl of 900 seconds so no need to reset or compare any times
  def locked = {
    failedMatchingAttempts == 3
  }

  def redirect(request: Request[AnyContent]): Option[Future[Result]] = {
    import shapeless.syntax.std.tuple._
    import shapeless.syntax.typeable._
    import uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.controllers.routes
    import scala.concurrent.Future
    import play.api.mvc.Results.Redirect
    val argList: List[Option[_]] =
      MatchingJourneySession
        .unapply(this)
        .map(_.toList)
        .fold(List.empty[Any])(identity)
        .map(_.cast[Option[_]])
        .filter(_.nonEmpty)
        .flatten
    val routeList: List[String] = List(
      routes.VrnController.get().url,
      routes.PostCodeController.get().url,
      routes.VatReturnController.get().url,
      routes.VatPeriodController.get().url,
      routes.VatRegistrationDateController.get().url
    )
    val route =
      argList
      .zip(routeList).zipWithIndex
      .find({case (x,i) => x._1.isEmpty && i < routeList.indexOf(request.uri)})
      .fold(request.uri)({case (a,_) => a._2})
    if (route != request.uri)
      Some(Future.successful(Redirect(route).withSession(request.session)))
    else None
  }

}

object MatchingJourneySession {
  implicit val formats = Json.format[MatchingJourneySession]
}

