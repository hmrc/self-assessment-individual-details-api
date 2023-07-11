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
import api.models.errors.MtdError
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.models.request.RetrieveItsaStatusRequest

trait MockRetrieveItsaStatusValidatorFactory extends MockFactory {

  val mockRetrieveItsaStatusValidatorFactory: RetrieveItsaStatusValidatorFactory = mock[RetrieveItsaStatusValidatorFactory]

  object MockedRetrieveItsaStatusValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveItsaStatusRequest]] = {
      (mockRetrieveItsaStatusValidatorFactory.validator(_: String, _: String, _: Option[String], _: Option[String])).expects(*, *, *, *)
    }

  }

  def willUseValidator(use: Validator[RetrieveItsaStatusRequest]): CallHandler[Validator[RetrieveItsaStatusRequest]] = {
    MockedRetrieveItsaStatusValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def successValidator(result: RetrieveItsaStatusRequest): Validator[RetrieveItsaStatusRequest] = new Validator[RetrieveItsaStatusRequest] {
    protected def validate: Either[Seq[MtdError], RetrieveItsaStatusRequest] = Right(result)
  }

  def errorValidator(result: MtdError): Validator[RetrieveItsaStatusRequest] = errorValidator(List(result))

  def errorValidator(result: Seq[MtdError]): Validator[RetrieveItsaStatusRequest] = new Validator[RetrieveItsaStatusRequest] {
    protected def validate: Either[Seq[MtdError], RetrieveItsaStatusRequest] = Left(result)
  }

}
