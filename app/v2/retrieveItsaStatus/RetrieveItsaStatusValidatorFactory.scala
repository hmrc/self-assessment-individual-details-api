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

import shared.controllers.validators.Validator
import v2.retrieveItsaStatus.def1.Def1_RetrieveItsaStatusValidator
import v2.retrieveItsaStatus.model.request.RetrieveItsaStatusRequestData

import javax.inject.Singleton

@Singleton
class RetrieveItsaStatusValidatorFactory {

  def validator(nino: String, taxYear: String, futureYears: Option[String], history: Option[String]): Validator[RetrieveItsaStatusRequestData] =
    new Def1_RetrieveItsaStatusValidator(nino, taxYear, futureYears, history)

}