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

package v2.retrieveItsaStatus.def1.model.response

import play.api.libs.json.{JsError, JsPath, Json}
import shared.utils.UnitSpec
import v2.models.domain.StatusEnum.{`MTD Mandated`, `No Status`}
import v2.models.domain.StatusReasonEnum.{`ITSA Q4 declaration`, `Sign up - return available`}

class Def1_RetrieveItsaStatusResponseSpec extends UnitSpec {

  "The RetrieveItsaStatusResponse JSON writes" should {
    "parse a top-level JSON array with IFS status and statusReason format" in {
      val json =
        s"""
           |[
           |  {
           |    "taxYear": "2021-22",
           |    "itsaStatusDetails": [
           |      {
           |        "submittedOn": "2023-06-01T10:19:00.303Z",
           |        "status": "No Status",
           |        "statusReason": "Sign up - return available",
           |        "businessIncomePriorTo2Years": 99999999999.99
           |      }
           |    ]
           |  },
           |  {
           |    "taxYear": "2020-21",
           |    "itsaStatusDetails": [
           |      {
           |        "submittedOn": "2022-05-01T10:19:00.101Z",
           |        "status": "MTD Mandated",
           |        "statusReason": "ITSA Q4 declaration",
           |        "businessIncomePriorTo2Years": 8.88
           |      }
           |    ]
           |  },
           |  {
           |    "taxYear": "2019-20",
           |    "itsaStatusDetails": [
           |    ]
           |  },
           |  {
           |    "taxYear": "2018-19"
           |  }
           |]
           |""".stripMargin

      val result =
        Json
          .parse(json)
          .as[Def1_RetrieveItsaStatusResponse]

      result shouldBe Def1_RetrieveItsaStatusResponse(itsaStatuses = List(
        ItsaStatuses(
          "2021-22",
          Some(
            List(
              ItsaStatusDetails("2023-06-01T10:19:00.303Z", `No Status`, `Sign up - return available`, Some(BigDecimal("99999999999.99")))
            ))),
        ItsaStatuses(
          "2020-21",
          Some(
            List(
              ItsaStatusDetails("2022-05-01T10:19:00.101Z", `MTD Mandated`, `ITSA Q4 declaration`, Some(BigDecimal("8.88")))
            ))),
        ItsaStatuses("2019-20", Some(Nil)),
        ItsaStatuses("2018-19", None)
      ))

    }

    "parse a top-level JSON array with HIP status and statusReason format" in {
      val json =
        """
           |[
           |  {
           |    "taxYear": "2021-22",
           |    "itsaStatusDetails": [
           |      {
           |        "submittedOn": "2023-06-01T10:19:00.303Z",
           |        "status": "00",
           |        "statusReason": "00",
           |        "businessIncomePriorTo2Years": 99999999999.99
           |      }
           |    ]
           |  },
           |  {
           |    "taxYear": "2020-21",
           |    "itsaStatusDetails": [
           |      {
           |        "submittedOn": "2022-05-01T10:19:00.101Z",
           |        "status": "01",
           |        "statusReason": "03",
           |        "businessIncomePriorTo2Years": 8.88
           |      }
           |    ]
           |  },
           |  {
           |    "taxYear": "2019-20",
           |    "itsaStatusDetails": [
           |    ]
           |  },
           |  {
           |    "taxYear": "2018-19"
           |  }
           |]
           |""".stripMargin

      val result =
        Json
          .parse(json)
          .as[Def1_RetrieveItsaStatusResponse]

      result shouldBe Def1_RetrieveItsaStatusResponse(itsaStatuses = List(
        ItsaStatuses(
          "2021-22",
          Some(
            List(
              ItsaStatusDetails("2023-06-01T10:19:00.303Z", `No Status`, `Sign up - return available`, Some(BigDecimal("99999999999.99")))
            ))),
        ItsaStatuses(
          "2020-21",
          Some(
            List(
              ItsaStatusDetails("2022-05-01T10:19:00.101Z", `MTD Mandated`, `ITSA Q4 declaration`, Some(BigDecimal("8.88")))
            ))),
        ItsaStatuses("2019-20", Some(Nil)),
        ItsaStatuses("2018-19", None)
      ))

    }

    "fail on an invalid response" in {
      val json =
        s"""
           |[
           |  {
           |    "itsaStatusDetails": [
           |      {
           |        "submittedOn": "2023-06-01T10:19:00.303Z",
           |        "status": "No Status",
           |        "statusReason": "Sign up - return available",
           |        "businessIncome2YearsPrior": 99999999999.99
           |      }
           |    ]
           |  }
           |]
           |""".stripMargin

      val result =
        Json
          .parse(json)
          .validate[Def1_RetrieveItsaStatusResponse]

      result shouldBe a[JsError]
      val expectedPath = JsPath \ 0 \ "taxYear"
      result.asInstanceOf[JsError].errors.head._1 shouldBe expectedPath
    }

  }

}
