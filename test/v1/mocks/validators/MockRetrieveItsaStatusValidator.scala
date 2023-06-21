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

package v1.mocks.validators

import api.models.errors.{ErrorWrapper, MtdError}
import api.models.request.NinoTaxYearFutureYearsHistoryRawData
import org.scalamock.handlers.{CallHandler, CallHandler1}
import org.scalamock.scalatest.MockFactory
import v1.controllers.requestParsers.validators.RetrieveItsaStatusValidator
import v1.models.request.RetrieveItsaStatusRequest

trait MockRetrieveItsaStatusValidator extends MockFactory {

  val mockRetrieveItsaStatusValidator: RetrieveItsaStatusValidator = mock[RetrieveItsaStatusValidator]

  object MockedRetrieveItsaStatusValidator {

    def parseAndValidateRequest(data: NinoTaxYearFutureYearsHistoryRawData): CallHandler[Either[ErrorWrapper, RetrieveItsaStatusRequest]] = {
      (mockRetrieveItsaStatusValidator
        .parseAndValidateRequest(_: NinoTaxYearFutureYearsHistoryRawData)(_: String))
        .expects(data, *)
    }

    def parseAndValidate(data: NinoTaxYearFutureYearsHistoryRawData): CallHandler1[NinoTaxYearFutureYearsHistoryRawData, Either[Seq[MtdError], RetrieveItsaStatusRequest]] = {

      (mockRetrieveItsaStatusValidator
        .parseAndValidate(_: NinoTaxYearFutureYearsHistoryRawData))
        .expects(data)
    }

  }

}
