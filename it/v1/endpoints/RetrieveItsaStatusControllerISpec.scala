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

package v1.endpoints

import api.models.domain.TaxYear
import api.models.errors._
import api.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class RetrieveItsaStatusControllerISpec extends IntegrationBaseSpec {

  "Calling the 'Retrieve ITSA Status' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        val ifsQueryParam: Map[String, String] = Map("futureYears" -> "false", "history" -> "false")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, ifsUri, ifsQueryParam, OK, ifsResponse)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String       = requestNino
            override val mtdTaxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2023-24", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2023-24", BAD_REQUEST, FutureYearsFormatError),
          ("AA123456A", "2023-24", BAD_REQUEST, HistoryFormatError)
        )
        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "ifs service error" when {
        def serviceErrorTest(ifsStatus: Int, ifsCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"ifs returns an $ifsCode error and status $ifsStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, ifsUri, ifsStatus, errorBody(ifsCode))
            }

            val response: WSResponse = await(request.get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |   "code": "$code",
             |   "reason": "ifs message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_FUTURES_YEAR", BAD_REQUEST, FutureYearsFormatError),
          (BAD_REQUEST, "INVALID_HISTORY", BAD_REQUEST, HistoryFormatError),
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
        )
        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }

  private trait Test {

    val nino: String              = "AA123456A"
    val mtdTaxYear: String        = "2023-24"
    val downstreamTaxYear: String = TaxYear.fromMtd(mtdTaxYear).asTysDownstream

    val ifsResponse: JsValue = Json.parse(
      """
        |
        |[
        |  {
        |    "taxYear": "2019-20",
        |    "itsaStatusDetails": [
        |      {
        |        "submittedOn": "2023-05-23T12:29:27.566Z",
        |        "status": "No Status",
        |        "statusReason": "Sign up - return available",
        |        "businessIncomePriorTo2Years": 23600.99
        |      }
        |    ]
        |  }
        |]
    """.stripMargin
    )

    val mtdResponse: JsValue = Json.parse(
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

    def uri: String = s"/itsa-status/$nino/$mtdTaxYear"

    def ifsUri: String = s"/income-tax/$nino/person-itd/itsa-status/$downstreamTaxYear"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

}