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

sealed trait ItsaStatusDetails

case class IfsItsaStatusDetails(submittedOn: String,
                                status: StatusEnum,
                                statusReason: StatusReasonEnum,
                                businessIncome2YearsPrior: Option[BigDecimal])
    extends ItsaStatusDetails

object IfsItsaStatusDetails {
  implicit val format: OFormat[IfsItsaStatusDetails] = Json.format[IfsItsaStatusDetails]
}

case class HipItsaStatusDetails(submittedOn: String,
                                status: StatusEnum,
                                statusReason: StatusReasonEnum,
                                businessIncome2YearsPrior: Option[BigDecimal])
    extends ItsaStatusDetails

object HipItsaStatusDetails {

  implicit val formats: OFormat[HipItsaStatusDetails] = new OFormat[HipItsaStatusDetails] {

    val hipReads: Reads[HipItsaStatusDetails] = (
      (__ \ "submittedOn").read[String] and
        (__ \ "status").read[StatusEnum](StatusEnum.hipFormat) and
        (__ \ "statusReason").read[StatusReasonEnum](StatusReasonEnum.hipFormat) and
        (__ \ "businessIncomePriorTo2Years").readNullable[BigDecimal]
    )(HipItsaStatusDetails.apply _)

    override def writes(o: HipItsaStatusDetails): JsObject = Json.writes.writes(o)

    override def reads(json: JsValue): JsResult[HipItsaStatusDetails] = json.validate[HipItsaStatusDetails](hipReads)
  }

}
