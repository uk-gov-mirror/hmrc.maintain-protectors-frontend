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

import models.protectors.IndividualProtector
import models.{CombinedPassportOrIdCard, IdCard, NationalInsuranceNumber, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.individual._
import play.api.libs.json.JsPath

import java.time.LocalDate
import scala.util.Try

class IndividualProtectorExtractor extends ProtectorExtractor[IndividualProtector] {

  override def apply(answers: UserAnswers, individual: IndividualProtector, index: Int): Try[UserAnswers] = {
    super.apply(answers, individual, index)
      .flatMap(_.set(NamePage, individual.name))
      .flatMap(answers => extractDateOfBirth(individual, answers))
      .flatMap(answers => extractAddress(individual.address, answers))
      .flatMap(answers => extractIdentification(individual, answers))
  }

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def countryOfResidenceUkYesNoPage: QuestionPage[Boolean] = CountryOfResidenceUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage
  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage

  override def startDatePage: QuestionPage[LocalDate] = StartDatePage

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.individual.basePath

  private def extractDateOfBirth(individual: IndividualProtector, answers: UserAnswers): Try[UserAnswers] = {
    individual.dateOfBirth match {
      case Some(dob) => answers
        .set(DateOfBirthYesNoPage, true)
        .flatMap(_.set(DateOfBirthPage, dob))
      case None => answers
        .set(DateOfBirthYesNoPage, false)
    }
  }

  private def extractIdentification(individual: IndividualProtector,
                                    answers: UserAnswers): Try[UserAnswers] = {
    individual.identification match {
      case Some(NationalInsuranceNumber(nino)) => answers
        .set(NationalInsuranceNumberYesNoPage, true)
        .flatMap(_.set(NationalInsuranceNumberPage, nino))
      case Some(p: Passport) => answers
        .set(NationalInsuranceNumberYesNoPage, false)
        .flatMap(_.set(PassportDetailsYesNoPage, true))
        .flatMap(_.set(PassportDetailsPage, p))
      case Some(id: IdCard) => answers
        .set(NationalInsuranceNumberYesNoPage, false)
        .flatMap(_.set(IdCardDetailsYesNoPage, true))
        .flatMap(_.set(IdCardDetailsPage, id))
      case Some(combined: CombinedPassportOrIdCard) => answers
        .set(NationalInsuranceNumberYesNoPage, false)
        .flatMap(_.set(PassportOrIdCardDetailsYesNoPage, true))
        .flatMap(_.set(PassportOrIdCardDetailsPage, combined))
      case _ => answers
        .set(NationalInsuranceNumberYesNoPage, false)
    }
  }
}
