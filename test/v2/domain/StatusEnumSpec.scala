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

package v2.domain

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import v2.models.domain.StatusEnum
import v2.models.domain.StatusEnum._

class StatusEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[StatusEnum](
    ("No Status", `No Status`),
    ("MTD Mandated", `MTD Mandated`),
    ("MTD Voluntary", `MTD Voluntary`),
    ("Annual", Annual),
    ("Non Digital", `Non Digital`),
    ("Dormant", Dormant),
    ("MTD Exempt", `MTD Exempt`)
  )

}
