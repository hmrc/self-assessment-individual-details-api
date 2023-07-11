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

package api.controllers.validators.resolvers

import api.models.domain.TaxYear
import api.models.errors.{RuleTaxYearRangeInvalid, TaxYearFormatError}
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class ResolveTaxYearSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "passed a valid tax year" in {
        val validTaxYear = "2018-19"
        val result       = ResolveTaxYear(validTaxYear)
        result shouldBe Right(TaxYear.fromMtd(validTaxYear))
      }
    }

    "return an error" when {
      "passed an invalid tax year format" in {
        val invalidTaxYear = "2019"
        val result         = ResolveTaxYear(invalidTaxYear)
        result shouldBe Left(List(TaxYearFormatError))
      }
    }

    "passed a tax year string in which the range is greater than 1 year" in {
      val invalidTaxYear = "2017-19"
      val result         = ResolveTaxYear(invalidTaxYear)
      result shouldBe Left(List(RuleTaxYearRangeInvalid))
    }

    "the end year is before the start year" in {
      val invalidTaxYear = "2018-17"
      val result         = ResolveTaxYear(invalidTaxYear)
      result shouldBe Left(List(RuleTaxYearRangeInvalid))
    }

    "the start and end years are the same" in {
      val invalidTaxYear = "2017-17"
      val result         = ResolveTaxYear(invalidTaxYear)
      result shouldBe Left(List(RuleTaxYearRangeInvalid))
    }

    "the tax year is bad" in {
      val invalidTaxYear = "20177-17"
      val result         = ResolveTaxYear(invalidTaxYear)
      result shouldBe Left(List(TaxYearFormatError))
    }
  }

}
