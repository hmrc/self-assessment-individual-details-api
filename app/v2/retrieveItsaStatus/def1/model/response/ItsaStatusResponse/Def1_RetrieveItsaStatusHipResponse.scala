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

package v2.retrieveItsaStatus.def1.model.response.ItsaStatusResponse

import play.api.libs.json._
import v2.retrieveItsaStatus.def1.model.response.HipItsaStatuses
import v2.retrieveItsaStatus.model.response.RetrieveItsaStatusResponse

case class Def1_RetrieveItsaStatusHipResponse(itsaStatuses: Seq[HipItsaStatuses]) extends RetrieveItsaStatusResponse

object Def1_RetrieveItsaStatusHipResponse {

  implicit val writes: OWrites[Def1_RetrieveItsaStatusHipResponse] = Json.writes[Def1_RetrieveItsaStatusHipResponse]

  implicit val reads: Reads[Def1_RetrieveItsaStatusHipResponse] = json =>
    json
      .validate[Seq[HipItsaStatuses]]
      .map(itsaStatuses => Def1_RetrieveItsaStatusHipResponse(itsaStatuses))

}
