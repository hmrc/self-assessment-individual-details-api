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

package v2.models.domain

import play.api.libs.json.{Format, JsonValidationError, Reads}
import shared.utils.enums.Enums
import shared.utils.enums.Enums.typeName
import shared.utils.enums.Values.MkValues

sealed trait StatusReasonEnum {
  val hipValue: String
}

object StatusReasonEnum {
  val parser: PartialFunction[String, StatusReasonEnum] = Enums.parser[StatusReasonEnum]
  implicit val format: Format[StatusReasonEnum]         = Enums.format[StatusReasonEnum]

  private def reads: Reads[StatusReasonEnum] = {
    val hipParser: PartialFunction[String, StatusReasonEnum] =
      implicitly[MkValues[StatusReasonEnum]].values.map(e => e.hipValue -> e).toMap

    implicitly[Reads[String]].collect(JsonValidationError(s"error.expected.$typeName"))(hipParser)
  }

  val hipFormat: Format[StatusReasonEnum] = Format(reads, Enums.writes[StatusReasonEnum])

  case object `Sign up - return available` extends StatusReasonEnum {
    val hipValue: String = "00"
  }

  case object `Sign up - no return available` extends StatusReasonEnum {
    override val hipValue: String = "01"
  }

  case object `ITSA final declaration` extends StatusReasonEnum {
    override val hipValue: String = "02"
  }

  case object `ITSA Q4 declaration` extends StatusReasonEnum {
    override val hipValue: String = "03"
  }

  case object `CESA SA return` extends StatusReasonEnum {
    override val hipValue: String = "04"
  }

  case object Complex extends StatusReasonEnum {
    override val hipValue: String = "05"
  }

  case object `Ceased income source` extends StatusReasonEnum {
    override val hipValue: String = "06"
  }

  case object `Reinstated income source` extends StatusReasonEnum {
    override val hipValue: String = "07"
  }

  case object Rollover extends StatusReasonEnum {
    override val hipValue: String = "08"
  }

  case object `Income Source Latency Changes` extends StatusReasonEnum {
    override val hipValue: String = "09"
  }

  case object `MTD ITSA Opt-Out` extends StatusReasonEnum {
    override val hipValue: String = "10"
  }

  case object `MTD ITSA Opt-In` extends StatusReasonEnum {
    override val hipValue: String = "11"
  }

  case object `Digitally Exempt` extends StatusReasonEnum {
    override val hipValue: String = "12"
  }

}
