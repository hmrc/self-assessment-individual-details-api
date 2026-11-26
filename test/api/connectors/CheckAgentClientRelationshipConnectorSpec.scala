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

package api.connectors

import api.config.AppConfig
import api.mocks.MockHttpClient
import play.api.http.Status.OK
import play.api.Configuration
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future

class CheckAgentClientRelationshipConnectorSpec extends ConnectorSpec {

  private val arn  = "TARN1234567"
  private val nino = "AA123456A"

  class Test extends MockHttpClient {

    private val configuration = Configuration(
      "microservice.services.agent-client-relationships.host" -> "test-host",
      "microservice.services.agent-client-relationships.port" -> 1234
    )

    private val servicesConfig = new ServicesConfig(configuration)
    private val appConfig      = new AppConfig(servicesConfig, configuration)
    val relationshipBaseUrl    = servicesConfig.baseUrl("agent-client-relationships")

    val connector = new CheckAgentClientRelationshipConnector(
      http = mockHttpClient,
      appConfig = appConfig
    )

  }

  "getAgentClientRelationship" should {
    "return the response from the agent client relationships service" in new Test {
      val response = HttpResponse(OK, "")

      MockedHttpClient
        .get[HttpResponse](
          url"$relationshipBaseUrl/agent/$arn/service/IR-SA/client/ni/$nino",
          dummyHeaderCarrierConfig
        )
        .returns(Future.successful(response))

      val result = await(connector.getAgentClientRelationship(arn, nino))

      result shouldBe response
    }
  }

}
