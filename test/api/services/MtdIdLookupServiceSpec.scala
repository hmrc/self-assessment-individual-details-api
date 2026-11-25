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

import api.config.MockAppConfig
import api.connectors.{MockMtdIdLookupConnector, MtdIdLookupConnector}
import api.models.errors.*
import api.services.MtdIdLookupService.Outcome

import scala.concurrent.Future

class MtdIdLookupServiceSpec extends ServiceSpec with MockAppConfig with MockNinoAuthService {

  val nino = "AA123456A"

  trait Test extends MockMtdIdLookupConnector {
    lazy val target = new MtdIdLookupService(mockMtdIdLookupConnector, mockAppConfig, mockNinoAuthService)
  }

  "calling .getMtdId" when {

    "an mtdId is found for the NINO" should {
      "return the mtdId" in new Test {
        val mtdId = "someMtdId"
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Right(mtdId))
        val result: Outcome = await(target.lookup(nino))

        result shouldBe Right(mtdId)
      }
    }

    "an invalid NINO is passed in" should {
      "return NinoFormatError" in new Test {
        val invalidNino     = "INVALID_NINO"
        val result: Outcome = await(target.lookup(invalidNino))

        result shouldBe Left(NinoFormatError)
      }
    }

    "the downstream service returns a 403 status error" should {
      "return ClientOrAgentNotAuthorisedError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(FORBIDDEN)))
        val result: Outcome = await(target.lookup(nino))

        result shouldBe Left(ClientOrAgentNotAuthorisedError)
      }
    }

    "the downstream service returns a 401 status error" should {
      "return InvalidBearerTokenError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNAUTHORIZED)))
        val result: Outcome = await(target.lookup(nino))

        result shouldBe Left(InvalidBearerTokenError)
      }
    }

    "the downstream service returns a 422 status error" should {
      "return ClientNotEnrolledError" when {
        "additional-nino-validation is disabled " in new Test {
          MockedAppConfig.featureSwitchConfig returns play.api.Configuration("additional-nino-validation" -> false)
          MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNPROCESSABLE_ENTITY)))
          val result: Outcome = await(target.lookup(nino))

          result shouldBe Left(ClientNotEnrolledError)
        }
        "additional-nino-validation is enabled and nino is authorised" in new Test {
          MockedAppConfig.featureSwitchConfig returns play.api.Configuration("additional-nino-validation" -> true)
          MockedNinoAuthService.checkAuthorisedNino(nino) returns Future.successful(ClientNotEnrolledError)
          MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNPROCESSABLE_ENTITY)))
          val result: Outcome = await(target.lookup(nino))

          result shouldBe Left(ClientNotEnrolledError)
        }
      }

      "return ClientOrAgentNotAuthorisedError" when {
        "additional-nino-validation is enabled and nino is not authorised" in new Test {
          MockedAppConfig.featureSwitchConfig returns play.api.Configuration("additional-nino-validation" -> true)
          MockedNinoAuthService.checkAuthorisedNino(nino) returns Future.successful(ClientOrAgentNotAuthorisedError)
          MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(UNPROCESSABLE_ENTITY)))
          val result: Outcome = await(target.lookup(nino))

          result shouldBe Left(ClientOrAgentNotAuthorisedError)
        }
      }
    }

    "the downstream service returns another status code" should {
      "return InternalError" in new Test {
        MockedMtdIdLookupConnector.lookup(nino) returns Future.successful(Left(MtdIdLookupConnector.Error(IM_A_TEAPOT)))
        val result: Outcome = await(target.lookup(nino))

        result shouldBe Left(InternalError)
      }
    }

  }

}
