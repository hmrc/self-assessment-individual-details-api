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

package api.controllers.validators.validations

import api.models.errors.BadRequestError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class BooleanValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when the string true is supplied" in {
        val validBoolean = "true"
        val result = BooleanValidation.validate(validBoolean, BadRequestError)
        result shouldBe Nil
      }

      "when the upper case string TRUE is supplied" in {
        val validBoolean = "TRUE"
        val result = BooleanValidation.validate(validBoolean, BadRequestError)
        result shouldBe Nil
      }

      "when the string false is supplied" in {
        val validBoolean = "false"
        val result = BooleanValidation.validate(validBoolean, BadRequestError)
        result shouldBe Nil
      }

      "when the optional string true is supplied" in {
        val validBoolean = Some("true")
        val result = BooleanValidation.validate(validBoolean, BadRequestError)
        result shouldBe Nil
      }
    }

    "return an error" when {
      "when an invalid string is supplied" in {
        val invalidBoolean = "invalid"
        val result = BooleanValidation.validate(invalidBoolean, BadRequestError)
        result shouldBe List(BadRequestError)
      }

      "when an invalid optional string is supplied" in {
        val invalidBoolean = Some("invalid")
        val result = BooleanValidation.validate(invalidBoolean, BadRequestError)
        result shouldBe List(BadRequestError)
      }
    }
  }

}
