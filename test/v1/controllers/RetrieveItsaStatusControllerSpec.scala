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

package v1.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, FutureYearsFormatError, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import v1.controllers.validators.MockRetrieveItsaStatusValidatorFactory
import v1.models.domain.{StatusEnum, StatusReasonEnum}
import v1.models.request.RetrieveItsaStatusRequest
import v1.models.response.{ItsaStatusDetails, ItsaStatuses, RetrieveItsaStatusResponse}
import v1.services.MockRetrieveItsaStatusService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveItsaStatusService
    with MockRetrieveItsaStatusValidatorFactory
    with MockHateoasFactory
    with HateoasLinks {

  private val taxYear = "2023-24"

  private val requestData = RetrieveItsaStatusRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
    futureYears = false,
    history = false
  )

  private val itsaStatusDetails = ItsaStatusDetails(
    submittedOn = "2023-05-23T12:29:27.566Z",
    status = StatusEnum.`No Status`,
    statusReason = StatusReasonEnum.`Sign up - return available`,
    businessIncomePriorTo2Years = Some(23600.99)
  )

  private val itsaStatuses = ItsaStatuses(
    taxYear = taxYear,
    itsaStatusDetails = Some(Seq(itsaStatusDetails))
  )

  private val responseDomainObject = RetrieveItsaStatusResponse(itsaStatuses = Seq(itsaStatuses))

  private val mtdResponse = Json.parse(
    """
      |{
      |  "itsaStatuses": [
      |    {
      |      "taxYear": "2023-24",
      |      "itsaStatusDetails": [
      |        {
      |          "submittedOn": "2023-05-23T12:29:27.566Z",
      |          "status": "No Status",
      |          "statusReason": "Sign up - return available",
      |          "businessIncomePriorTo2Years": 23600.99
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "RetrieveItsaStatusController" should {
    "return OK" when {
      "a valid request is made" in new Test {
        willUseValidator(successValidator(requestData))

        MockRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseDomainObject))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponse))
      }
    }

    "return the error as per spec" when {
      "the validation fails" in new Test {
        willUseValidator(errorValidator(NinoFormatError))
        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(successValidator(requestData))

        MockRetrieveItsaStatusService
          .retrieve(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, FutureYearsFormatError))))

        runErrorTest(FutureYearsFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    private val controller = new RetrieveItsaStatusController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveItsaStatusValidatorFactory,
      service = mockRetrieveItsaStatusService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveItsaStatus(nino, taxYear, None, None)(fakeGetRequest)

  }

}
