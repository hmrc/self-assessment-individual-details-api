/*
 * Copyright 2025 HM Revenue & Customs
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

package v2.retrieveItsaStatus.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v2.models.domain.{StatusEnum, StatusReasonEnum}

case class ItsaStatusDetails(submittedOn: String, status: StatusEnum, statusReason: StatusReasonEnum, businessIncome2YearsPrior: Option[BigDecimal])

object ItsaStatusDetails {

  implicit val reads: Reads[ItsaStatusDetails] = (
    (JsPath \ "submittedOn").read[String] and
      (JsPath \ "status").read[StatusEnum] and
      (JsPath \ "statusReason").read[StatusReasonEnum] and
      (JsPath \ "businessIncomePriorTo2Years").readNullable[BigDecimal]
  )(ItsaStatusDetails.apply _)

  implicit val writes: OWrites[ItsaStatusDetails] = Json.writes[ItsaStatusDetails]

}
