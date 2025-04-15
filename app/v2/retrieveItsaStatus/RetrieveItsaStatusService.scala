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

package v2.retrieveItsaStatus

import cats.implicits._
import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v2.models.errors.{FutureYearsFormatError, HistoryFormatError}
import model.request.RetrieveItsaStatusRequestData
import model.response.RetrieveItsaStatusResponse
import connectors.{RetrieveItsaStatusIfsConnector, RetrieveItsaStatusHipConnector}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveItsaStatusService @Inject() (ifsConnector: RetrieveItsaStatusIfsConnector, hipConnector: RetrieveItsaStatusHipConnector)(implicit
    appConfig: SharedAppConfig)
    extends BaseService {

  def retrieve(request: RetrieveItsaStatusRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveItsaStatusResponse]] = {

    if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1878")) {
      hipConnector
        .retrieve(request)
        .map(_.leftMap(mapDownstreamErrors(hipErrorMap)))
    } else {
      ifsConnector
        .retrieve(request)
        .map(_.leftMap(mapDownstreamErrors(ifsErrorMap)))
    }

  }

  private val ifsErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_FUTURES_YEAR"      -> FutureYearsFormatError,
      "INVALID_HISTORY"           -> HistoryFormatError,
      "INVALID_CORRELATION_ID"    -> InternalError,
      "NOT_FOUND"                 -> NotFoundError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

  private val hipErrorMap: Map[String, MtdError] =
    ifsErrorMap ++ Map(
      "1215" -> NinoFormatError,
      "1117" -> TaxYearFormatError,
      "1216" -> InternalError,
      "5000" -> NinoFormatError,
      "5001" -> NinoFormatError,
      "5009" -> InternalError,
      "5010" -> NotFoundError
    )

}
