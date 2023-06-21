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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.controllers.validators.Validator.{ParserValidationCaller, PostParseValidationCallers, PreParseValidationCallers}
import api.controllers.validators.validations.{BooleanValidation, NinoValidation, TaxYearValidation}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{FutureYearsFormatError, HistoryFormatError}
import v1.models.request.{RetrieveItsaStatusRawData, RetrieveItsaStatusRequest}

import javax.inject.Singleton

@Singleton
class RetrieveItsaStatusValidator extends Validator[RetrieveItsaStatusRawData, RetrieveItsaStatusRequest] {

  protected val preParserValidations: PreParseValidationCallers[RetrieveItsaStatusRawData] =
    List(
      data => NinoValidation(data.nino),
      data => TaxYearValidation(data.taxYear),
      data => BooleanValidation.validate(data.futureYears, FutureYearsFormatError),
      data => BooleanValidation.validate(data.history, HistoryFormatError)
    )

  protected val parserValidation: ParserValidationCaller[RetrieveItsaStatusRawData, RetrieveItsaStatusRequest] = { data =>
    Right(RetrieveItsaStatusRequest(Nino(data.nino), TaxYear.fromMtd(data.taxYear), data.futureYears.contains("true"), data.history.contains("true")))
  }

  protected val postParserValidations: PostParseValidationCallers[RetrieveItsaStatusRequest] = Nil

}
