/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.vatdeferralnewpaymentschemefrontend.models.iv

import uk.gov.hmrc.http.HttpResponse
import play.api.libs.json.Json

sealed trait IvResponse

sealed trait IvSuccessResponse extends IvResponse

object IvSuccessResponse {

  case object Success extends IvSuccessResponse

  case object Incomplete extends IvSuccessResponse

  case object FailedMatching extends IvSuccessResponse

  case object FailedIV extends IvSuccessResponse

  case object InsufficientEvidence extends IvSuccessResponse

  case object LockedOut extends IvSuccessResponse

  case object UserAborted extends IvSuccessResponse

  case object Timeout extends IvSuccessResponse

  case object TechnicalIssue extends IvSuccessResponse

  case object PrecondFailed extends IvSuccessResponse

  def fromString(s: String): Option[IvSuccessResponse] = // scalastyle:ignore cyclomatic.complexity
    s match {
      case "Success" ⇒ Some(Success)
      case "Incomplete" ⇒ Some(Incomplete)
      case "FailedMatching" ⇒ Some(FailedMatching)
      case "FailedIV" ⇒ Some(FailedIV)
      case "InsufficientEvidence" ⇒ Some(InsufficientEvidence)
      case "LockedOut" ⇒ Some(LockedOut)
      case "UserAborted" ⇒ Some(UserAborted)
      case "Timeout" ⇒ Some(Timeout)
      case "TechnicalIssue" ⇒ Some(TechnicalIssue)
      case "PreconditionFailed" ⇒ Some(PrecondFailed)
      case _ ⇒ None

    }

}


case class IvUnexpectedResponse(r: HttpResponse) extends IvResponse

case class IvErrorResponse(cause: Exception) extends IvResponse