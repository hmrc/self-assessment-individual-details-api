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

package api.controllers.validators

import api.models.errors.{BadRequestError, ErrorWrapper, MtdError}
import utils.Logging

trait Validator[PARSED] extends Logging {

  protected def validate: Either[Seq[MtdError], PARSED]

  def validateAndWrapResult()(implicit correlationId: String): Either[ErrorWrapper, PARSED] = {
    validate match {
      case Right(parsed) =>
        logger.info(s"Validation successful for the request with CorrelationId: $correlationId")
        Right(parsed)

      case Left(err :: Nil) =>
        logger.warn(s"Validation failed with ${err.code} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, err, None))

      case Left(errs) =>
        logger.warn(s"Validation failed with ${errs.map(_.code).mkString(",")} error for the request with CorrelationId: $correlationId")
        Left(ErrorWrapper(correlationId, BadRequestError, Some(errs)))
    }
  }

  protected def mapResult(result: Either[Seq[MtdError], PARSED], possibleErrors: Either[Seq[MtdError], _]*): Either[Seq[MtdError], PARSED] = {
    result match {
      case Left(_)       => combineLefts(possibleErrors)
      case Right(parsed) => Right(parsed)
    }
  }

  private def combineLefts(possibleErrors: Seq[Either[Seq[MtdError], _]]): Either[Seq[MtdError], PARSED] =
    Left(
      possibleErrors
        .collect { case Left(errs) => errs }
        .flatten
        .toList)

}
