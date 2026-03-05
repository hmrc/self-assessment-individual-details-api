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

package shared.connectors

import cats.data.EitherT
import play.api.http.Status.*
import play.api.mvc.Request
import shared.utils.Logging
import shared.config.SharedAppConfig
import shared.models.errors.{ClientNotEnrolledError, ClientOrAgentNotAuthorisedError, InternalError, MtdError}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class EnrolmentsAuthConnector @Inject() (http: HttpClientV2, appConfig: SharedAppConfig) {

  val baseUrl: String = appConfig.enrolmentStoreProxyUrl

  def getMtdIds(
      mtdId: String
  )(implicit
      hc: HeaderCarrier,
      request: Request[?]
  ): EitherT[Future, UpstreamErrorResponse, MtdError] = {
    val url = s"$baseUrl/enrolment-store/enrolments/HMRC-MTD-IT~MTDITID~$mtdId/groups"
    read(
      http
        .get(url"$url")
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
    )
      .map { response =>
        response.status match {
          case OK         => ClientOrAgentNotAuthorisedError
          case NO_CONTENT => ClientNotEnrolledError
          case _          => InternalError
        }
      }
  }

  def read(
      response: Future[Either[UpstreamErrorResponse, HttpResponse]]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    EitherT(response)

}
