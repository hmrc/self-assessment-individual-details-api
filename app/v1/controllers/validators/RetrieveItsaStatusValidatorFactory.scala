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
import api.controllers.validators.resolvers.{ResolveBoolean, ResolveNino, ResolveTaxYear}
import api.models.errors.{FutureYearsFormatError, HistoryFormatError, MtdError}
import v1.models.request.RetrieveItsaStatusRequest

import javax.inject.Singleton

@Singleton
class RetrieveItsaStatusValidatorFactory {

  def validator(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]): Validator[RetrieveItsaStatusRequest] =
    new Validator[RetrieveItsaStatusRequest] {

      protected def validate: Either[Seq[MtdError], RetrieveItsaStatusRequest] = {
        val resolvedNino        = ResolveNino(nino)
        val resolvedTaxYear     = ResolveTaxYear(taxYear)
        val resolvedFutureYears = ResolveBoolean(futureYears, defaultValue = false, FutureYearsFormatError)
        val resolvedHistory     = ResolveBoolean(history, defaultValue = false, HistoryFormatError)

        val result: Either[Seq[MtdError], RetrieveItsaStatusRequest] = for {
          nino        <- resolvedNino
          taxYear     <- resolvedTaxYear
          futureYears <- resolvedFutureYears
          history     <- resolvedHistory
        } yield {
          RetrieveItsaStatusRequest(nino, taxYear, futureYears, history)
        }

        mapResult(result, possibleErrors = resolvedNino, resolvedTaxYear, resolvedFutureYears, resolvedHistory)
      }

    }

}
