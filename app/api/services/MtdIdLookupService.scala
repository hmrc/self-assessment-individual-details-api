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

package api.services

import api.config.AppConfig
import api.connectors.MtdIdLookupConnector
import api.controllers.validators.resolvers.ResolveNino
import api.models.errors.*
import play.api.http.Status.*
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object MtdIdLookupService {
  type Outcome = Either[MtdError, String]
}

@Singleton
class MtdIdLookupService @Inject() (val connector: MtdIdLookupConnector, appConfig: AppConfig, ninoAuthService: NinoAuthService) {

  def lookup(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MtdIdLookupService.Outcome] = {
    if (!ResolveNino.isValid(nino)) {
      Future.successful(Left(NinoFormatError))
    } else {
      connector.getMtdId(nino).flatMap {
        case Right(mtdId) => Future.successful(Right(mtdId))
        case Left(MtdIdLookupConnector.Error(statusCode)) =>
          statusCode match {
            case FORBIDDEN    => Future.successful(Left(ClientOrAgentNotAuthorisedError))
            case UNAUTHORIZED => Future.successful(Left(InvalidBearerTokenError))
            case UNPROCESSABLE_ENTITY =>
              if (appConfig.featureSwitchConfig.get[Boolean]("additional-nino-validation")) {
                ninoAuthService.checkAuthorisedNino(nino).map(Left(_))
              } else {
                Future.successful(Left(ClientNotEnrolledError))
              }
            case _ => Future.successful(Left(InternalError))
          }
      }
    }
  }

}
