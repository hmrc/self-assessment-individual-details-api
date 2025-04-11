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

sealed trait StatusEnum {
  val hipValue: String
}

object StatusEnum {
  val parser: PartialFunction[String, StatusEnum] = Enums.parser[StatusEnum]
  implicit val format: Format[StatusEnum]         = Enums.format[StatusEnum]

  private def reads: Reads[StatusEnum] = {
    val hipParser: PartialFunction[String, StatusEnum] =
      implicitly[MkValues[StatusEnum]].values.map(e => e.hipValue -> e).toMap

    implicitly[Reads[String]].collect(JsonValidationError(s"error.expected.$typeName"))(hipParser)
  }

  val hipFormat: Format[StatusEnum] = Format(reads, Enums.writes[StatusEnum])

  case object `No Status` extends StatusEnum {
    override val hipValue: String = "00"
  }

  case object `MTD Mandated` extends StatusEnum {
    override val hipValue: String = "01"
  }

  case object `MTD Voluntary` extends StatusEnum {
    override val hipValue: String = "02"
  }

  case object Annual extends StatusEnum {
    override val hipValue: String = "03"
  }

  case object `Non Digital` extends StatusEnum {
    override val hipValue: String = "04"
  }

  case object Dormant extends StatusEnum {
    override val hipValue: String = "05"
  }

  case object `MTD Exempt` extends StatusEnum {
    override val hipValue: String = "99"
  }

}
