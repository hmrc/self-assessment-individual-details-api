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

package v2.retrieveItsaStatus

import shared.config.{ConfigFeatureSwitches, SharedAppConfig}
import shared.connectors.DownstreamUri.{HipUri, IfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome, DownstreamUri}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import v2.retrieveItsaStatus.model.request.RetrieveItsaStatusRequestData
import v2.retrieveItsaStatus.model.response.RetrieveItsaStatusResponse

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RetrieveItsaStatusConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieve(request: RetrieveItsaStatusRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveItsaStatusResponse]] = {

    import request._
    import schema._

    val downstreamUri: DownstreamUri[DownstreamResp] = if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1878")) {
      HipUri[DownstreamResp](
        s"itsd/person-itd/itsa-status/$nino?taxYear=${taxYear.asTysDownstream}&futureYears=$futureYears&history=$history"
      )
    } else {
      IfsUri[DownstreamResp](
        s"income-tax/$nino/person-itd/itsa-status/${taxYear.asTysDownstream}?futureYears=$futureYears&history=$history"
      )
    }

    get(downstreamUri)
  }

}
