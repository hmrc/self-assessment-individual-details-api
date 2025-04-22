package v2.retrieveItsaStatus

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

package v2.retrieveItsaStatus

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.models.domain.StatusEnum.`No Status`
import v2.models.domain.StatusReasonEnum.`Sign up - return available`
import v2.retrieveItsaStatus.def1.model.request.Def1_RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.def1.model.response.{Def1_RetrieveItsaStatusResponse, IfsItsaStatusDetails, IfsItsaStatuses}
import v2.retrieveItsaStatus.model.response.RetrieveItsaStatusResponse

import scala.concurrent.Future

class RetrieveItsaStatusIfsConnectorSpec extends ConnectorSpec {

  private val nino    = "AA111111A"
  private val taxYear = TaxYear.fromMtd("2023-24")

  private val itsaStatusDetails = ItsaStatusDetails("2023-05-23T12:29:27.566Z", `No Status`, `Sign up - return available`, Some(23600.99))
  private val itsaStatuses      = HipItsaStatuses(taxYear.asMtd, Some(List(itsaStatusDetails)))
  private val responseModel     = Def1_RetrieveItsaStatusResponse(List(itsaStatuses))

  private val outcome = Right(ResponseWrapper(correlationId, responseModel))

  "RetrieveItsaStatusConnector" should {
    "return a 200 status and expected response for a success scenario" in new IfsTest with Test {

      willGet(url = s"$baseUrl/income-tax/$nino/person-itd/itsa-status/${taxYear.asTysDownstream}?futureYears=true&history=true")
        .returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveItsaStatusResponse] = await(connector.retrieve(request))

      result shouldBe outcome
    }
  }

  private trait Test {
    _: ConnectorTest =>

    val connector: RetrieveItsaStatusIfsConnector = new RetrieveItsaStatusIfsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    val request: Def1_RetrieveItsaStatusRequestData = Def1_RetrieveItsaStatusRequestData(Nino(nino), taxYear, futureYears = true, history = true)

  }

}
