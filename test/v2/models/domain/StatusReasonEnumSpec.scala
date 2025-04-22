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

import shared.utils.UnitSpec
import shared.utils.enums.EnumJsonSpecSupport
import v2.models.domain.StatusReasonEnum
import v2.models.domain.StatusReasonEnum._

class StatusReasonEnumSpec extends UnitSpec with EnumJsonSpecSupport {

  testDeserialization[StatusReasonEnum](
    ("00", `Sign up - return available`),
    ("01", `Sign up - no return available`),
    ("02", `ITSA final declaration`),
    ("03", `ITSA Q4 declaration`),
    ("04", `CESA SA return`),
    ("05", Complex),
    ("06", `Ceased income source`),
    ("07", `Reinstated income source`),
    ("08", Rollover),
    ("09", `Income Source Latency Changes`),
    ("10", `MTD ITSA Opt-Out`),
    ("11", `MTD ITSA Opt-In`),
    ("12", `Digitally Exempt`)
  )

  testRoundTrip[StatusReasonEnum](
    ("Sign up - return available", `Sign up - return available`),
    ("Sign up - no return available", `Sign up - no return available`),
    ("ITSA final declaration", `ITSA final declaration`),
    ("ITSA Q4 declaration", `ITSA Q4 declaration`),
    ("CESA SA return", `CESA SA return`),
    ("Complex", Complex),
    ("Ceased income source", `Ceased income source`),
    ("Reinstated income source", `Reinstated income source`),
    ("Rollover", Rollover),
    ("Income Source Latency Changes", `Income Source Latency Changes`),
    ("MTD ITSA Opt-Out", `MTD ITSA Opt-Out`),
    ("MTD ITSA Opt-In", `MTD ITSA Opt-In`),
    ("Digitally Exempt", `Digitally Exempt`)
  )

}
