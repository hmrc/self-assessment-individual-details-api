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

package v2.retrieveItsaStatus.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsObject, JsResult, JsValue, Json, OFormat, Reads, __}
import v2.models.domain.{StatusEnum, StatusReasonEnum}

case class ItsaStatusDetails(submittedOn: String, status: StatusEnum, statusReason: StatusReasonEnum, businessIncome2YearsPrior: Option[BigDecimal])

object ItsaStatusDetails {

  implicit val formats: OFormat[ItsaStatusDetails] = new OFormat[ItsaStatusDetails] {

    val hipReads: Reads[ItsaStatusDetails] = (
      (__ \ "submittedOn").read[String] and
        (__ \ "status").read[StatusEnum] and
        (__ \ "statusReason").read[StatusReasonEnum] and
        (__ \ "businessIncomePriorTo2Years").readNullable[BigDecimal]
    )(ItsaStatusDetails.apply _)

    override def writes(o: ItsaStatusDetails): JsObject = Json.writes.writes(o)

    override def reads(json: JsValue): JsResult[ItsaStatusDetails] = json.validate[ItsaStatusDetails](hipReads)
  }

}
