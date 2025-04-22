/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.retrieveItsaStatus.model.response

import play.api.libs.json.{JsObject, Json, OWrites}
import shared.utils.JsonWritesUtil
import v2.retrieveItsaStatus.def1.model.response.{Def1_RetrieveItsaStatusHipResponse, Def1_RetrieveItsaStatusIfsResponse}

trait RetrieveItsaStatusResponse

object RetrieveItsaStatusResponse extends JsonWritesUtil {

  implicit val writes: OWrites[RetrieveItsaStatusResponse] = writesFrom {
    case def1: Def1_RetrieveItsaStatusIfsResponse => Json.toJson(def1).as[JsObject]
    case def1: Def1_RetrieveItsaStatusHipResponse => Json.toJson(def1).as[JsObject]
  }

}
