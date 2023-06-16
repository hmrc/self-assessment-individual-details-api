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

sealed trait StatusEnum {
  def toDesViewString: String
}

object StatusEnum {

  case object noStatus extends StatusEnum {
    override def toDesViewString: String = "No Status"
  }

  case object mtdMandated extends StatusEnum {
    override def toDesViewString: String = "MTD Mandated"
  }

  case object mtdVoluntary extends StatusEnum {
    override def toDesViewString: String = "MTD Voluntary"
  }

  case object annual extends StatusEnum {
    override def toDesViewString: String = "Annual"
  }

  case object nonDigital extends StatusEnum {
    override def toDesViewString: String = "Non Digital"
  }

  case object dormant extends StatusEnum {
    override def toDesViewString: String = "Dormant"
  }

  case object mtdExempt extends StatusEnum {
    override def toDesViewString: String = "MTD Exempt"
  }

  implicit val format: Format[StatusEnum]         = Enums.format[StatusEnum]
  val parser: PartialFunction[String, StatusEnum] = Enums.parser[StatusEnum]
}
