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

package shared.services

import shared.config.SharedAppConfig
import shared.connectors.EnrolmentsAuthConnector
import shared.models.auth.UserDetails
import shared.models.errors.*
import shared.models.outcomes.AuthOutcome
import shared.utils.Logging
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.*
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject() (val connector: AuthConnector,
                                       val enrolmentsAuthConnector: EnrolmentsAuthConnector,
                                       val appConfig: SharedAppConfig)
    extends Logging {

  import shared.models.errors.InternalError

  private lazy val authorisationEnabled = appConfig.confidenceLevelConfig.authValidationEnabled

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  private def initialPredicate(mtdId: String): Predicate =
    if (authorisationEnabled) {
      EnrolmentsAuthService.authorisationEnabledPredicate(mtdId)
    } else {
      EnrolmentsAuthService.authorisationDisabledPredicate(mtdId)
    }

  def authorised(mtdId: String, endpointAllowsSupportingAgents: Boolean = false)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[AuthOutcome] = {
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    println(Console.RED_B + "New Test" + Console.RESET)
    authFunction
      .authorised(initialPredicate(mtdId))
      .retrieve(affinityGroup and authorisedEnrolments and allEnrolments) {
        case Some(Individual) ~ _ ~ enrolments =>
          println(Console.RED_B + "Individual success" + Console.RESET)
          Future.successful(Right(UserDetails(mtdId, "Individual", None)))
        case Some(Organisation) ~ _ ~ enrolments =>
          println(Console.RED_B + "Organisation success" + Console.RESET)
          Future.successful(Right(UserDetails(mtdId, "Organisation", None)))
        case Some(Agent) ~ authorisedEnrolments ~ enrolments =>
          println(Console.RED_B + "Agent Hit" + Console.RESET)
          authFunction
            .authorised(EnrolmentsAuthService.mtdEnrolmentPredicate(mtdId)) {
              println(Console.RED_B + "Agent Success" + Console.RESET)
              Future.successful(agentDetails(mtdId, authorisedEnrolments, "Agent"))
            }
            .recoverWith {
              case _: AuthorisationException if endpointAllowsSupportingAgents =>
                println(Console.RED_B + "Supporting Agent" + Console.RESET)
                authFunction
                  .authorised(EnrolmentsAuthService.supportingAgentAuthPredicate(mtdId)) {
                    Future.successful(agentDetails(mtdId, authorisedEnrolments, "Supporting Agent"))
                  }
              case _: InsufficientEnrolments =>
                println(Console.RED_B + "Agent InsufficientEnrolments" + Console.RESET)
                logger.warn(s"[EnrolmentsAuthService][authorised] Agent enrolment not found for MTDITID: $mtdId")
                Future.successful(Left(NinoFormatError))
            }
        case _ =>
          logger.warn(s"[EnrolmentsAuthService][authorised] Invalid AffinityGroup.")
          println(Console.RED_B + "Non Agent, Org or Individual" + Console.RESET)
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
      }
      .recoverWith {
        case _: InsufficientEnrolments =>
          println(Console.RED_B + "General InsufficientEnrolments" + Console.RESET)
          logger.warn(s"[EnrolmentsAuthService][authorised] Insufficient Enrolments")
          insufficientEnrolments(mtdId)
        case _: AuthorisationException =>
          println(Console.RED_B + "General Fail AuthorisationException" + Console.RESET)
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
        case error =>
          println(Console.RED_B + "General Catch All" + Console.RESET)
          logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
          Future.successful(Left(InternalError))
      }
  }

  private def agentDetails(mtdId: String, enrolments: Enrolments, agentType: String): Either[MtdError, UserDetails] = {
    (for {
      enrolment  <- enrolments.getEnrolment("HMRC-AS-AGENT")
      identifier <- enrolment.getIdentifier("AgentReferenceNumber")
    } yield UserDetails(mtdId, agentType, Some(identifier.value)))
      .toRight {
        logger.warn("[EnrolmentsAuthService][authorised] No AgentReferenceNumber defined on agent enrolment.")
        InternalError
      }
  }

  private def insufficientEnrolments(mtdId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {
    authFunction
      .authorised(initialPredicate(mtdId))
      .retrieve(allEnrolments) {
        case enrolments: Enrolments =>
          if enrolments.enrolments.contains(Enrolment("HMRC-MTD-IT")) then {
            println(Console.RED_B + "New!!" + Console.RESET)
            Future.successful(Left(ClientOrAgentNotAuthorisedError))
          } else {
            println(Console.RED_B + "New!!2" + Console.RESET)
            println(Console.RED_B + enrolmentsAuthConnector.getMtdIds(mtdId).map(Left(_)) + Console.RESET)
            enrolmentsAuthConnector.getMtdIds(mtdId).map(Left(_))
          }
        case null =>
          Future.successful(Left(InternalError))
      }
      .recoverWith {
        case _: AuthorisationException =>
          println(Console.RED_B + "General Fail AuthorisationException 2" + Console.RESET)
          Future.successful(Left(ClientOrAgentNotAuthorisedError))
        case error =>
          println(Console.RED_B + "General Catch All 2" + Console.RESET)
          logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
          Future.successful(Left(InternalError))
      }
  }

}

object EnrolmentsAuthService {

  private[services] def authorisationEnabledPredicate(mtdId: String): Predicate =
    (Individual and ConfidenceLevel.L200 and mtdEnrolmentPredicate(mtdId)) or
      (Organisation and mtdEnrolmentPredicate(mtdId)) or
      (Agent and Enrolment("HMRC-AS-AGENT"))

  private[services] def authorisationDisabledPredicate(mtdId: String): Predicate =
    mtdEnrolmentPredicate(mtdId) or (Agent and Enrolment("HMRC-AS-AGENT"))

  private[services] def mtdEnrolmentPredicate(mtdId: String): Enrolment = {
    Enrolment("HMRC-MTD-IT")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth")
  }

  private[services] def supportingAgentAuthPredicate(mtdId: String): Enrolment = {
    Enrolment("HMRC-MTD-IT-SUPP")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth-supp")
  }

}
