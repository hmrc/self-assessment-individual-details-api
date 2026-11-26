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

import api.connectors.CheckAgentClientRelationshipConnector
import api.models.errors.*
import api.utils.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, nino}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NinoAuthService @Inject() (val connector: CheckAgentClientRelationshipConnector,
                                 val authorisationConnector: AuthConnector,
                                 val enrolmentsAuthService: EnrolmentsAuthService)
    extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = authorisationConnector
  }

  def checkAuthorisedNino(userNino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MtdError] = {
    authFunction
      .authorised(EnrolmentsAuthService.additionalNinoAuthorisationPredicate)
      .retrieve(affinityGroup and authorisedEnrolments and nino) {
        case Some(Individual | Organisation) ~ _ ~ Some(nino) =>
          if (nino == userNino) {
            Future.successful(ClientNotEnrolledError)
          } else {
            Future.successful(ClientOrAgentNotAuthorisedError)
          }
        case Some(Agent) ~ enrolments ~ _ =>
          determineAgentClientRelationship(enrolments, userNino)
        case _ ~ _ ~ _ =>
          Future.successful(ClientOrAgentNotAuthorisedError)
      }
      .recoverWith {
        case _: AuthorisationException =>
          Future.successful(ClientOrAgentNotAuthorisedError)
        case error =>
          logger.warn(s"[NinoAuthService][checkAuthorisedNino] An unexpected error occurred: $error")
          Future.successful(InternalError)
      }
  }

  private def determineAgentClientRelationship(enrolments: Enrolments, userNino: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[MtdError] = {
    enrolments
      .getEnrolment("HMRC-AS-AGENT")
      .flatMap(_.getIdentifier("AgentReferenceNumber"))
      .map(_.value) match {
      case Some(arn) =>
        connector.getAgentClientRelationship(arn, userNino).map {
          _.status match {
            case OK => ClientNotEnrolledError
            case status if status == NOT_FOUND =>
              ClientOrAgentNotAuthorisedError
            case _ =>
              InternalError
          }
        }
      case None =>
        Future.successful(ClientOrAgentNotAuthorisedError)

    }
  }

}
