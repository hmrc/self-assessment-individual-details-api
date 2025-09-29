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

import shared.utils.UnitSpec
import v2.retrieveItsaStatus.def1.Def1_RetrieveItsaStatusValidator
import v2.retrieveItsaStatus.def2.Def2_RetrieveItsaStatusValidator

import java.time.{Clock, Instant, ZoneId}

class RetrieveItsaStatusValidatorFactorySpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  "validator factory" when {
    "given any request for schema and the tax year is 2025-26 or before" should {
      val validatorFactory = new RetrieveItsaStatusValidatorFactory
      "return the Validator for schema definition 1" in {
        validatorFactory.validator(validNino, validTaxYear, None, None) shouldBe a[Def1_RetrieveItsaStatusValidator]
      }

      "return the Validator for schema definition when request specifies futureYears and history and the tax year is 2025-26 or before" in {
        validatorFactory.validator(validNino, validTaxYear, Some("true"), Some("true")) shouldBe a[Def1_RetrieveItsaStatusValidator]

      }
    }

    "given any request for schema and the tax year is 2026-27 or after" should {
      implicit val clock: Clock = Clock.fixed(Instant.parse("2026-07-11T10:00:00.00Z"), ZoneId.of("UTC"))
      val validatorFactory      = new RetrieveItsaStatusValidatorFactory
      "return the Validator for schema definition 2" in {
        validatorFactory.validator(validNino, validTaxYear, None, None) shouldBe a[Def2_RetrieveItsaStatusValidator]
      }

      "return the Validator for schema definition when request specifies futureYears and history and the tax year is 2026-27 or after" in {
        validatorFactory.validator(validNino, validTaxYear, Some("true"), Some("true")) shouldBe a[Def2_RetrieveItsaStatusValidator]

      }
    }

  }

}
