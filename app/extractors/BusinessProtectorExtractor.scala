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

import models.protectors.BusinessProtector
import models.{NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.business._
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.Try

class BusinessProtectorExtractor extends ProtectorExtractor[BusinessProtector] {

  override def apply(answers: UserAnswers, business: BusinessProtector, index: Int): Try[UserAnswers] = {
    super.apply(answers, business, index)
      .flatMap(_.set(NamePage, business.name))
      .flatMap(answers => extractAddress(business.address, answers))
      .flatMap(answers => extractUtr(business.utr, answers))
  }

  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = AddressUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def startDatePage: QuestionPage[LocalDate] = StartDatePage

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.individual.basePath

  private def extractUtr(utr: Option[String], answers: UserAnswers): Try[UserAnswers] = {
    utr match {
      case Some(utr) => answers
        .set(UtrYesNoPage, true)
        .flatMap(_.set(UtrPage, utr))
      case _ => answers
        .set(UtrYesNoPage, false)
    }
  }
}
