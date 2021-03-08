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

package extractors

import models.protectors.Protector
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.Try

trait ProtectorExtractor[T <: Protector] {

  def apply(answers: UserAnswers, protector: T, index: Int): Try[UserAnswers] = {
    answers.deleteAtPath(basePath)
      .flatMap(_.set(startDatePage, protector.entityStart))
      .flatMap(_.set(indexPage, index))
  }

  def addressYesNoPage: QuestionPage[Boolean]
  def ukAddressYesNoPage: QuestionPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def startDatePage: QuestionPage[LocalDate]

  def indexPage: QuestionPage[Int]

  def basePath: JsPath

  def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    address match {
      case Some(uk: UkAddress) => answers
        .set(addressYesNoPage, true)
        .flatMap(_.set(ukAddressYesNoPage, true))
        .flatMap(_.set(ukAddressPage, uk))
      case Some(nonUk: NonUkAddress) => answers
        .set(addressYesNoPage, true)
        .flatMap(_.set(ukAddressYesNoPage, false))
        .flatMap(_.set(nonUkAddressPage, nonUk))
      case _ => answers
        .set(addressYesNoPage, false)
    }
  }

}
