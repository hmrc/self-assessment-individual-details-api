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

package v2.retrieveItsaStatus.connectors

import play.api.libs.json.Reads
import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.retrieveItsaStatus.RetrieveItsaStatusSchema.IfsDef1
import v2.retrieveItsaStatus.def1.model.response.ItsaStatusResponse.Def1_RetrieveItsaStatusIfsResponse
import v2.retrieveItsaStatus.model.request.RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.model.response.RetrieveItsaStatusResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveItsaStatusIfsConnector @Inject() (val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveItsaStatusRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveItsaStatusResponse]] = {
    import request._

    implicit val schema: Reads[Def1_RetrieveItsaStatusIfsResponse] = IfsDef1.connectorReads

    get(IfsUri(s"income-tax/$nino/person-itd/itsa-status/${taxYear.asTysDownstream}?futureYears=$futureYears&history=$history"))
  }

}
