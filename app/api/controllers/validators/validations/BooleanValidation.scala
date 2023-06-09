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

package api.controllers.validators.validations

import api.models.errors.MtdError

import scala.util.{Failure, Success, Try}

object BooleanValidation {

  def validate(value: Option[String], error: MtdError): List[MtdError] = {
    value.map(validate(_, error)).getOrElse(Nil)
  }

  def validate(value: String, error: MtdError): List[MtdError] = Try {
    value.toBoolean
  } match {
    case Success(_) => Nil
    case Failure(_) => List(error)
  }

}
