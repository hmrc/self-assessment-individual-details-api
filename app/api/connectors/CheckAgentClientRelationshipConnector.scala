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

package api.connectors

import api.config.AppConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckAgentClientRelationshipConnector @Inject() (http: HttpClientV2, appConfig: AppConfig) {

  def getAgentClientRelationship(arn: String, nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    http.get(url"${appConfig.config.baseUrl("agent-client-relationships")}/agent/$arn/service/IR-SA/client/ni/$nino").execute[HttpResponse]
  }

}
