/*
 * Copyright 2021 HM Revenue & Customs
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

package utils.mappers

import models.protectors.Protector
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.reflect.{ClassTag, classTag}

abstract class Mapper[T <: Protector : ClassTag] extends Logging {

  def apply(answers: UserAnswers): Option[T] = {
    answers.data.validate[T](reads) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[UTR: ${answers.utr}] Failed to rehydrate ${classTag[T].runtimeClass.getSimpleName} from UserAnswers due to $errors")
        None
    }
  }

  val reads: Reads[T]

  def addressDeciderPage: QuestionPage[Boolean]
  def addressYesNoPage: QuestionPage[Boolean]
  def ukAddressYesNoPage: QuestionPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def readAddress: Reads[Option[Address]] = {
    addressDeciderPage.path.read[Boolean].flatMap {
      case true => Reads(_ => JsSuccess(None))
      case false => addressYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
        case true => readUkOrNonUkAddress
        case false => Reads(_ => JsSuccess(None))
      }
    }
  }

  private def readUkOrNonUkAddress: Reads[Option[Address]] = {
    ukAddressYesNoPage.path.read[Boolean].flatMap[Option[Address]] {
      case true => ukAddressPage.path.read[UkAddress].map(Some(_))
      case false => nonUkAddressPage.path.read[NonUkAddress].map(Some(_))
    }
  }

}
