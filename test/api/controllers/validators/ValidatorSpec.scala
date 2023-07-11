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

package api.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import org.scalamock.scalatest.MockFactory
import support.UnitSpec

class ValidatorSpec extends UnitSpec with MockFactory {

  private implicit val correlationId: String = "1234"

  private val validNino    = Nino("AA123456A")
  private val validTaxYear = TaxYear.fromMtd("2023-24")

  private val parsedRequestBody = TestParsedRequestBody("value 1", value2 = true)
  private val parsedRequest     = TestParsedRequest(validNino, validTaxYear, parsedRequestBody)

  case class TestParsedRequest(nino: Nino, taxYear: TaxYear, body: TestParsedRequestBody)
  case class TestParsedRequestBody(value1: String, value2: Boolean)

  class TestValidator extends Validator[TestParsedRequest] {
    protected def validate: Either[Seq[MtdError], TestParsedRequest] = Right(parsedRequest)
  }

  "validateAndWrapResult()" should {

    "return the parsed domain object" when {
      "given valid input" in {
        val validator = new TestValidator
        val result    = validator.validateAndWrapResult()
        result shouldBe Right(parsedRequest)
      }
    }

    "return an error from the request params" when {
      "given a single invalid request param" in {
        val validator = new TestValidator {
          override protected def validate = Left(List(NinoFormatError))
        }

        val result = validator.validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "given two invalid request params" in {
        val validator = new TestValidator {
          override protected def validate = Left(List(NinoFormatError, TaxYearFormatError))
        }

        val result = validator.validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }

    }
  }

}
