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

import models.Constant.GB
import models.protectors.Protector
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.{Success, Try}

trait ProtectorExtractor[T <: Protector] {

  def apply(answers: UserAnswers, protector: T, index: Int): Try[UserAnswers] = {
    answers.deleteAtPath(basePath)
      .flatMap(_.set(startDatePage, protector.entityStart))
      .flatMap(_.set(indexPage, index))
  }

  def countryOfResidenceYesNoPage: QuestionPage[Boolean]
  def countryOfResidenceUkYesNoPage: QuestionPage[Boolean]
  def countryOfResidencePage: QuestionPage[String]
  def addressYesNoPage: QuestionPage[Boolean]
  def ukAddressYesNoPage: QuestionPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress]

  def startDatePage: QuestionPage[LocalDate]

  def indexPage: QuestionPage[Int]

  def basePath: JsPath

  def extractCountryOfResidence(countryOfResidence: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.is5mldEnabled && answers.isUnderlyingData5mld) {
      countryOfResidence match {
        case Some(GB) => answers
          .set(countryOfResidenceYesNoPage, true)
          .flatMap(_.set(countryOfResidenceUkYesNoPage, true))
          .flatMap(_.set(countryOfResidencePage, GB))
        case Some(country) => answers
          .set(countryOfResidenceYesNoPage, true)
          .flatMap(_.set(countryOfResidenceUkYesNoPage, false))
          .flatMap(_.set(countryOfResidencePage, country))
        case None => answers
          .set(countryOfResidenceYesNoPage, false)
      }
    } else {
      Success(answers)
    }
  }

  def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
    if (answers.isTaxable) {
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
    } else {
      Success(answers)
    }
  }

}
