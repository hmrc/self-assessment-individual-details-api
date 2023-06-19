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

package v1.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait StatusReasonEnum {
  val downstreamValue: String
}

object StatusReasonEnum {

  case object signUpReturnAvailable extends StatusReasonEnum {
    val downstreamValue: String = "Sign up - return available"
  }

  case object signUpNoReturnAvailable extends StatusReasonEnum {
    val downstreamValue: String = "Sign up - no return available"
  }

  case object itsaFinalDeclaration extends StatusReasonEnum {
    val downstreamValue: String = "ITSA final declaration"
  }

  case object itsaQ4Declaration extends StatusReasonEnum {
    val downstreamValue: String = "ITSA Q4 declaration"
  }

  case object cesaSaReturn extends StatusReasonEnum {
    val downstreamValue: String = "CESA SA return"
  }

  case object complex extends StatusReasonEnum {
    val downstreamValue: String = "Complex"
  }

  case object ceasedIncomeSource extends StatusReasonEnum {
    val downstreamValue: String = "Ceased income source"
  }

  case object reinstatedIncomeSource extends StatusReasonEnum {
    val downstreamValue: String = "Reinstated income source"
  }

  case object rollover extends StatusReasonEnum {
    val downstreamValue: String = "Rollover"
  }

  implicit val format: Format[StatusReasonEnum]         = Enums.format[StatusReasonEnum]
  val parser: PartialFunction[String, StatusReasonEnum] = Enums.parser[StatusReasonEnum]
}
