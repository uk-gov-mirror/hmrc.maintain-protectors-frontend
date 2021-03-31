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

import models._
import models.protectors.IndividualProtector
import pages.QuestionPage
import pages.individual._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsSuccess, Reads}

import java.time.LocalDate

class IndividualProtectorMapper extends Mapper[IndividualProtector] {

  override val reads: Reads[IndividualProtector] = (
    NamePage.path.read[Name] and
      DateOfBirthPage.path.readNullable[LocalDate] and
      readIdentification and
      readAddress and
      StartDatePage.path.read[LocalDate] and
      Reads(_ => JsSuccess(true))
    )(IndividualProtector.apply _)

  private def readIdentification: Reads[Option[IndividualIdentification]] = {
    NationalInsuranceNumberYesNoPage.path.read[Boolean].flatMap[Option[IndividualIdentification]] {
      case true => NationalInsuranceNumberPage.path.read[String].map(nino => Some(NationalInsuranceNumber(nino)))
      case false => readPassportOrIdCard
    }
  }

  private def readPassportOrIdCard: Reads[Option[IndividualIdentification]] = {
    val identification = for {
      hasNino <- NationalInsuranceNumberYesNoPage.path.readWithDefault(false)
      hasAddress <- AddressYesNoPage.path.readWithDefault(false)
      hasPassport <- PassportDetailsYesNoPage.path.readWithDefault(false)
      hasIdCard <- IdCardDetailsYesNoPage.path.readWithDefault(false)
      hasPassportOrIdCard <- PassportOrIdCardDetailsYesNoPage.path.readWithDefault(false)
    } yield (hasNino, hasAddress, hasPassport, hasIdCard, hasPassportOrIdCard)

    identification.flatMap[Option[IndividualIdentification]] {
      case (false, true, true, false, _) => PassportDetailsPage.path.read[Passport].map(Some(_))
      case (false, true, false, true, _) => IdCardDetailsPage.path.read[IdCard].map(Some(_))
      case (false, true, false, false, true) => PassportOrIdCardDetailsPage.path.read[CombinedPassportOrIdCard].map(Some(_))
      case _ => Reads(_ => JsSuccess(None))
    }
  }

  override def countryOfResidenceYesNoPage: QuestionPage[Boolean] = CountryOfResidenceYesNoPage
  override def countryOfResidenceUkYesNoPage: QuestionPage[Boolean] = CountryOfResidenceUkYesNoPage
  override def countryOfResidencePage: QuestionPage[String] = CountryOfResidencePage
  override def addressDeciderPage: QuestionPage[Boolean] = NationalInsuranceNumberYesNoPage
  override def addressYesNoPage: QuestionPage[Boolean] = AddressYesNoPage
  override def ukAddressYesNoPage: QuestionPage[Boolean] = LiveInTheUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage
}
