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

package v1.domain

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import v1.models.domain.StatusReasonEnum

class StatusReasonEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[StatusReasonEnum](
    ("signUpReturnAvailable", StatusReasonEnum.signUpReturnAvailable),
    ("signUpNoReturnAvailable", StatusReasonEnum.signUpNoReturnAvailable),
    ("itsaFinalDeclaration", StatusReasonEnum.itsaFinalDeclaration),
    ("itsaQ4Declaration", StatusReasonEnum.itsaQ4Declaration),
    ("cesaSaReturn", StatusReasonEnum.cesaSaReturn),
    ("complex", StatusReasonEnum.complex),
    ("ceasedIncomeSource", StatusReasonEnum.ceasedIncomeSource),
    ("reinstatedIncomeSource", StatusReasonEnum.reinstatedIncomeSource),
    ("rollover", StatusReasonEnum.rollover)
  )

  "toDesViewString" must {
    "return the expected string" in {
      StatusReasonEnum.signUpReturnAvailable.toDesViewString shouldBe "Sign up - return available"
      StatusReasonEnum.signUpNoReturnAvailable.toDesViewString shouldBe "Sign up - no return available"
      StatusReasonEnum.itsaFinalDeclaration.toDesViewString shouldBe "ITSA final declaration"
      StatusReasonEnum.itsaQ4Declaration.toDesViewString shouldBe "ITSA Q4 declaration"
      StatusReasonEnum.cesaSaReturn.toDesViewString shouldBe "CESA SA return"
      StatusReasonEnum.complex.toDesViewString shouldBe "Complex"
      StatusReasonEnum.ceasedIncomeSource.toDesViewString shouldBe "Ceased income source"
      StatusReasonEnum.reinstatedIncomeSource.toDesViewString shouldBe "Reinstated income source"
      StatusReasonEnum.rollover.toDesViewString shouldBe "Rollover"
    }
  }

}
