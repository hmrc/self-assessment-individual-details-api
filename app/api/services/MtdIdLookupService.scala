/*
 * Copyright 2023 HM Revenue & Customs
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

package api.services

import api.connectors.MtdIdLookupConnector
import api.controllers.validators.resolvers.ResolveNino
import api.models.errors.*
import api.utils.Logging
import play.api.http.Status.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, Enrolment, Enrolments}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object MtdIdLookupService {
  type Outcome = Either[MtdError, String]
}

@Singleton
class MtdIdLookupService @Inject() (val connector: MtdIdLookupConnector, val authConn: AuthConnector, http: HttpClientV2) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = authConn
  }

  def lookup(ninoValue: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MtdIdLookupService.Outcome] = {
    if (!ResolveNino.isValid(ninoValue)) {
      Future.successful(Left(NinoFormatError))
    } else {
      connector.getMtdId(ninoValue) flatMap {
        case Right(mtdId) => Future.successful(Right(mtdId))
        case Left(MtdIdLookupConnector.Error(statusCode)) =>
          statusCode match {
            case FORBIDDEN =>
              logger.warn(s"[MtdIdLookupService][lookup] Received FORBIDDEN from MTD ID Lookup")
              Future.successful(Left(ClientOrAgentNotAuthorisedError))
            case UNAUTHORIZED =>
              logger.warn(s"[MtdIdLookupService][lookup] Received UNAUTHORIZED from MTD ID Lookup")
              Future.successful(Left(InvalidBearerTokenError))
            case UNPROCESSABLE_ENTITY =>
              logger.warn(s"[MtdIdLookupService][lookup] Received UNPROCESSABLE_ENTITY from MTD ID Lookup")
              handleClientNotEnrolled(ninoValue)
            case _ => Future.successful(Left(InternalError))
          }
      }
    }
  }

  private def handleClientNotEnrolled(ninoValue: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MtdIdLookupService.Outcome] = {
    val predicate: Predicate =
      (Individual and ConfidenceLevel.L250) or (Organisation and ConfidenceLevel.L250) or (Agent and Enrolment("HMRC-AS-AGENT"))

    authFunction
      .authorised(predicate)
      .retrieve(affinityGroup and nino and authorisedEnrolments) {
        case Some(Individual | Organisation) ~ authNinoValue ~ _ =>
          handleIndividualClientNotEnrolled(ninoValue, authNinoValue)
        case Some(Agent) ~ _ ~ authorisedEnrolments =>
          handleAgentClientNotEnrolled(ninoValue, authorisedEnrolments)
        case affinityGroup =>
          logger.warn(s"[MtdIdLookupService][handleClientNotEnrolled] Received unrecognised affinity group $affinityGroup from auth connector")
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
      }
      .recoverWith {
        case _: AuthorisationException =>
          logger.warn(s"[MtdIdLookupService][handleClientNotEnrolled] Received AuthorisationException from auth connector")
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
        case error => Future.successful(Left(InternalError))
      }
  }

  private def handleIndividualClientNotEnrolled(ninoValue: String, authNinoValue: Option[String]): Future[MtdIdLookupService.Outcome] = {
    if (authNinoValue.contains(ninoValue)) Future.successful(Left(ClientNotEnrolledError))
    else
      logger.warn(s"[MtdIdLookupService][handleIndividualClientNotEnrolled] Nino value $ninoValue does not match auth nino value $authNinoValue")
      Future.successful(Left(ClientOrAgentNotAuthorisedError))
  }

  private def handleAgentClientNotEnrolled(ninoValue: String, authorisedEnrolments: Enrolments)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[MtdIdLookupService.Outcome] = {

    implicit val reads: HttpReads[MtdIdLookupService.Outcome] = (_: String, url: String, response: HttpResponse) => {
      if (response.status == 200) Left(ClientNotEnrolledError)
      else if (response.status == 404)
        logger.warn(s"[MtdIdLookupService][handleAgentClientNotEnrolled] No agent client relationship found")
        Left(ClientOrAgentNotAuthorisedError)
      else
        logger.warn(
          s"[MtdIdLookupService][handleAgentClientNotEnrolled] Error from Agent Client Relationship API: Status $response.status, Body ${response.body}")
        Left(InternalError)
    }

    val maybeArn = for {
      enrolment  <- authorisedEnrolments.getEnrolment("HMRC-AS-AGENT")
      identifier <- enrolment.getIdentifier("AgentReferenceNumber")
    } yield identifier

    maybeArn match {
      case Some(arn) =>
        val service      = "IR-SA"
        val clientIdType = "ni"
        val clientId     = ninoValue

        http
          .get(url"http://localhost:9772/agent/${arn.value}/service/$service/client/$clientIdType/$clientId")(
            hc.withExtraHeaders(("Environment", "Local")))
          .execute
      case None =>
        logger.warn(s"[MtdIdLookupService][handleAgentClientNotEnrolled] No Agent ARN found")
        Future.successful(Left(ClientOrAgentNotAuthorisedError))
    }
  }

}
