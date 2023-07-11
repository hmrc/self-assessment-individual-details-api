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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.request.RetrieveItsaStatusRequest

class RetrieveItsaStatusValidatorFactorySpec extends UnitSpec with MockAppConfig {

  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  private val invalidNino    = "not-a-nino"
  private val invalidTaxYear = "23-24"

  private val validatorFactory = new RetrieveItsaStatusValidatorFactory()

  protected def validator(nino: String,
                          taxYear: String,
                          futureYears: Option[String] = None,
                          history: Option[String] = None): Validator[RetrieveItsaStatusRequest] =
    validatorFactory.validator(nino, taxYear, futureYears, history)

  "parseAndValidateRequest()" should {
    "return the parsed domain object" when {
      "the request is valid" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(RetrieveItsaStatusRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear), futureYears = false, history = false))
      }

      "the request is valid and specifies futureYears and history" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator(validNino, validTaxYear, Some("true"), Some("true")).validateAndWrapResult()

        result shouldBe Right(RetrieveItsaStatusRequest(Nino(validNino), TaxYear.fromMtd(validTaxYear), futureYears = true, history = true))
      }
    }

    "perform the validation and wrap the error in a response wrapper" when {

      "the request has one error" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] = validator(invalidNino, validTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "the request has multiple errors caught during preParse" in {
        val result: Either[ErrorWrapper, RetrieveItsaStatusRequest] =
          validator(invalidNino, invalidTaxYear, Some("not-boolean"), Some("not-boolean")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                NinoFormatError,
                TaxYearFormatError,
                FutureYearsFormatError,
                HistoryFormatError
              ))))
      }
    }
  }

}
