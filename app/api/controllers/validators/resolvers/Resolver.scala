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

package api.controllers.validators.resolvers

import api.models.errors.{InternalError, MtdError}

/** Parses a raw (String) value to a target type, validating in the process.
  *
  * @tparam T
  *   The target data type
  */
trait Resolver[T] {

  def apply(value: String, error: Option[MtdError]): Either[Seq[MtdError], T]

  def apply(value: String): Either[Seq[MtdError], T] =
    apply(value, error = None)

  def apply(value: String, error: MtdError): Either[Seq[MtdError], T] =
    apply(value, Option(error))

  def apply(maybeValue: Option[String], error: MtdError): Either[Seq[MtdError], Option[T]] =
    apply(maybeValue, Option(error))

  def apply(maybeValue: Option[String], defaultValue: T): Either[Seq[MtdError], T] =
    apply(maybeValue, defaultValue, error = None)

  def apply(maybeValue: Option[String], defaultValue: T, error: MtdError): Either[Seq[MtdError], T] =
    apply(maybeValue, defaultValue, Option(error))

  def apply(maybeValue: Option[String], defaultValue: T, error: Option[MtdError]): Either[Seq[MtdError], T] =
    apply(maybeValue, error)
      .map(maybeResolvedValue => maybeResolvedValue.getOrElse(defaultValue))

  def apply(maybeValue: Option[String]): Either[Seq[MtdError], Option[T]] =
    apply(maybeValue, error = None)

  def apply(maybeValue: Option[String], error: Option[MtdError]): Either[Seq[MtdError], Option[T]] =
    maybeValue match {
      case Some(value) => apply(value, error).map(Option(_))
      case None        => Right(None)
    }

  protected def requireError(maybeError: Option[MtdError]): MtdError = maybeError.getOrElse(InternalError)

}
