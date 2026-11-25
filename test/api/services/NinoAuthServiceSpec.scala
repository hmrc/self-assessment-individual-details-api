/*
 * Copyright 2026 HM Revenue & Customs
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
import org.scalamock.handlers.CallHandler
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolment, EnrolmentIdentifier, Enrolments, InsufficientEnrolments}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, nino}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class NinoAuthServiceSpec extends ServiceSpec {

  private val userNino             = "AA123456A"
  private val arn                  = "TARN1234567"
  private val predicate: Predicate = EnrolmentsAuthService.additionalNinoAuthorisationPredicate
  private val retrieval            = affinityGroup and authorisedEnrolments and nino

  "calling .checkAuthorisedNino" when {

    "the user is an individual" should {
      "return ClientNotEnrolledError when the authenticated NINO matches the user provided one" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(individualWithNino(userNino)))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientNotEnrolledError
      }

      "return ClientOrAgentNotAuthorisedError when the authenticated NINO does not match the user provided one" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(individualWithNino("BB123456B")))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientOrAgentNotAuthorisedError
      }
    }

    "the user is an organisation" should {
      "return ClientNotEnrolledError when the authenticated NINO matches the user provided one" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(organisationWithNino(userNino)))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientNotEnrolledError
      }

      "return ClientOrAgentNotAuthorisedError when the authenticated NINO does not match the user provided one" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(organisationWithNino("BB123456B")))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientOrAgentNotAuthorisedError
      }
    }

    "the user is an agent" should {
      "return ClientNotEnrolledError when the agent has an active relationship with the client" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(agentWithArn(arn)))
        MockedAgentClientRelationshipConnector
          .getAgentClientRelationship(arn, userNino)
          .returns(Future.successful(HttpResponse(OK, "")))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientNotEnrolledError
      }

      "return ClientOrAgentNotAuthorisedError when no relationship exists" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(agentWithArn(arn)))
        MockedAgentClientRelationshipConnector
          .getAgentClientRelationship(arn, userNino)
          .returns(Future.successful(HttpResponse(NOT_FOUND, "")))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientOrAgentNotAuthorisedError
      }

      "return InternalError when the relationship service returns another status" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(agentWithArn(arn)))
        MockedAgentClientRelationshipConnector
          .getAgentClientRelationship(arn, userNino)
          .returns(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

        await(target.checkAuthorisedNino(userNino)) shouldBe InternalError
      }

      "return ClientOrAgentNotAuthorisedError when the agent enrolment has no ARN" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(agentWithoutArn))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientOrAgentNotAuthorisedError
      }
    }

    "authorisation fails" should {
      "return ClientOrAgentNotAuthorisedError for an authorisation exception" in new Test {
        MockedAuthConnector
          .authorised(retrieval)
          .returns(Future.failed(InsufficientEnrolments()))

        await(target.checkAuthorisedNino(userNino)) shouldBe ClientOrAgentNotAuthorisedError
      }

      "return InternalError for an unexpected error" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.failed(new RuntimeException("unexpected")))

        await(target.checkAuthorisedNino(userNino)) shouldBe InternalError
      }
    }

    "the relationship service fails" should {
      "return InternalError" in new Test {
        MockedAuthConnector.authorised(retrieval).returns(Future.successful(agentWithArn(arn)))
        MockedAgentClientRelationshipConnector
          .getAgentClientRelationship(arn, userNino)
          .returns(Future.failed(new RuntimeException("unexpected")))

        await(target.checkAuthorisedNino(userNino)) shouldBe InternalError
      }
    }
  }

  private trait Test {
    private val mockAuthConnector: AuthConnector = mock[AuthConnector]

    private val mockAgentClientRelationshipConnector: CheckAgentClientRelationshipConnector =
      mock[CheckAgentClientRelationshipConnector]

    lazy val target = new NinoAuthService(
      mockAgentClientRelationshipConnector,
      mockAuthConnector,
      mock[EnrolmentsAuthService]
    )

    object MockedAuthConnector {

      def authorised[A](retrieval: Retrieval[A]): CallHandler[Future[A]] =
        (mockAuthConnector
          .authorise[A](_: Predicate, _: Retrieval[A])(_: HeaderCarrier, _: ExecutionContext))
          .expects(predicate, retrieval, *, *)

    }

    object MockedAgentClientRelationshipConnector {

      def getAgentClientRelationship(agentReferenceNumber: String, nino: String): CallHandler[Future[HttpResponse]] =
        (mockAgentClientRelationshipConnector
          .getAgentClientRelationship(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext))
          .expects(agentReferenceNumber, nino, *, *)

    }

  }

  private def individualWithNino(value: String): ~[~[Option[AffinityGroup], Enrolments], Option[String]] =
    new ~(new ~(Some(Individual), Enrolments(Set.empty)), Some(value))

  private def organisationWithNino(value: String): ~[~[Option[AffinityGroup], Enrolments], Option[String]] =
    new ~(new ~(Some(Organisation), Enrolments(Set.empty)), Some(value))

  private def agentWithArn(value: String): ~[~[Option[AffinityGroup], Enrolments], Option[String]] =
    new ~(
      new ~(
        Some(Agent),
        Enrolments(Set(Enrolment("HMRC-AS-AGENT", List(EnrolmentIdentifier("AgentReferenceNumber", value)), "Active")))
      ),
      None
    )

  private val agentWithoutArn: ~[~[Option[AffinityGroup], Enrolments], Option[String]] =
    new ~(new ~(Some(Agent), Enrolments(Set(Enrolment("HMRC-AS-AGENT", Nil, "Active")))), None)

}
