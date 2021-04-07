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

import base.SpecBase
import models.Constant.GB
import models.protectors.IndividualProtector
import models.{CombinedPassportOrIdCard, Name, NationalInsuranceNumber, NonUkAddress, UkAddress, UserAnswers}
import pages.individual._

import java.time.LocalDate

class IndividualProtectorExtractorSpec extends SpecBase {

  private val index = 0

  private val name = Name("Joe", None, "Bloggs")
  private val dateOfBirth = LocalDate.parse("1996-02-03")
  private val nino = "AA000000A"
  private val ukAddress = UkAddress("Line 1", "Line 2", None, None, "postcode")
  private val country = "FR"
  private val nonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val passportOrIdCard = CombinedPassportOrIdCard(country, "1234567890", LocalDate.parse("2020-12-25"))
  private val startDate = LocalDate.parse("2000-01-01")

  private val extractor = injector.instanceOf[IndividualProtectorExtractor]

  "IndividualProtectorExtractor" must {

    "Populate user answers" when {

      "4mld" when {

        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = false, isTaxable = true, isUnderlyingData5mld = false)

        "has minimal data" in {

          val protector = IndividualProtector(
            name = name,
            dateOfBirth = None,
            identification = None,
            address = None,
            entityStart = startDate,
            provisional = true
          )

          val result = extractor(baseAnswers, protector, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthYesNoPage).get mustBe false
          result.get(DateOfBirthPage) mustBe None
          result.get(NationalInsuranceNumberYesNoPage).get mustBe false
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(AddressYesNoPage).get mustBe false
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(StartDatePage).get mustBe startDate
        }

        "has date of birth and NINO" in {

          val protector = IndividualProtector(
            name = name,
            dateOfBirth = Some(dateOfBirth),
            identification = Some(NationalInsuranceNumber(nino)),
            address = None,
            entityStart = startDate,
            provisional = true
          )

          val result = extractor(baseAnswers, protector, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthYesNoPage).get mustBe true
          result.get(DateOfBirthPage).get mustBe dateOfBirth
          result.get(NationalInsuranceNumberYesNoPage).get mustBe true
          result.get(NationalInsuranceNumberPage).get mustBe nino
          result.get(AddressYesNoPage) mustBe None
          result.get(LiveInTheUkYesNoPage) mustBe None
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage) mustBe None
          result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(StartDatePage).get mustBe startDate
        }
        
        "has UK address" in {

          val protector = IndividualProtector(
            name = name,
            dateOfBirth = None,
            identification = None,
            address = Some(ukAddress),
            entityStart = startDate,
            provisional = true
          )

          val result = extractor(baseAnswers, protector, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthYesNoPage).get mustBe false
          result.get(DateOfBirthPage) mustBe None
          result.get(NationalInsuranceNumberYesNoPage).get mustBe false
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(AddressYesNoPage).get mustBe true
          result.get(LiveInTheUkYesNoPage).get mustBe true
          result.get(UkAddressPage).get mustBe ukAddress
          result.get(NonUkAddressPage) mustBe None
          result.get(PassportOrIdCardDetailsYesNoPage).get mustBe false
          result.get(PassportOrIdCardDetailsPage) mustBe None
          result.get(StartDatePage).get mustBe startDate
        }
        
        "has non-UK address and passport" in {

          val protector = IndividualProtector(
            name = name,
            dateOfBirth = None,
            identification = Some(passportOrIdCard),
            address = Some(nonUkAddress),
            entityStart = startDate,
            provisional = true
          )

          val result = extractor(baseAnswers, protector, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(DateOfBirthYesNoPage).get mustBe false
          result.get(DateOfBirthPage) mustBe None
          result.get(NationalInsuranceNumberYesNoPage).get mustBe false
          result.get(NationalInsuranceNumberPage) mustBe None
          result.get(AddressYesNoPage).get mustBe true
          result.get(LiveInTheUkYesNoPage).get mustBe false
          result.get(UkAddressPage) mustBe None
          result.get(NonUkAddressPage).get mustBe nonUkAddress
          result.get(PassportOrIdCardDetailsYesNoPage).get mustBe true
          result.get(PassportOrIdCardDetailsPage).get mustBe passportOrIdCard
          result.get(StartDatePage).get mustBe startDate
        }
      }

      "5mld" when {

        "underlying data is 4mld" when {

          val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

          "has minimal data" in {

            val protector = IndividualProtector(
              name = name,
              dateOfBirth = None,
              countryOfNationality = None,
              identification = None,
              countryOfResidence = None,
              address = None,
              mentalCapacityYesNo = None,
              entityStart = startDate,
              provisional = true
            )

            val result = extractor(baseAnswers, protector, index).get

            result.get(IndexPage).get mustBe index
            result.get(NamePage).get mustBe name
            result.get(DateOfBirthYesNoPage).get mustBe false
            result.get(DateOfBirthPage) mustBe None
            result.get(CountryOfNationalityYesNoPage) mustBe None
            result.get(CountryOfNationalityUkYesNoPage) mustBe None
            result.get(CountryOfNationalityPage) mustBe None
            result.get(NationalInsuranceNumberYesNoPage).get mustBe false
            result.get(NationalInsuranceNumberPage) mustBe None
            result.get(CountryOfResidenceYesNoPage) mustBe None
            result.get(CountryOfResidenceUkYesNoPage) mustBe None
            result.get(CountryOfResidencePage) mustBe None
            result.get(AddressYesNoPage).get mustBe false
            result.get(LiveInTheUkYesNoPage) mustBe None
            result.get(UkAddressPage) mustBe None
            result.get(NonUkAddressPage) mustBe None
            result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
            result.get(PassportOrIdCardDetailsPage) mustBe None
            result.get(MentalCapacityYesNoPage) mustBe None
            result.get(StartDatePage).get mustBe startDate
          }
        }

        "underlying data is 5mld" when {

          "taxable" when {

            val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = true)

            "has UK nationality, UK residency and is legally capable" in {

              val protector = IndividualProtector(
                name = name,
                dateOfBirth = None,
                countryOfNationality = Some(GB),
                identification = None,
                countryOfResidence = Some(GB),
                address = None,
                mentalCapacityYesNo = Some(true),
                entityStart = startDate,
                provisional = true
              )

              val result = extractor(baseAnswers, protector, index).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(DateOfBirthYesNoPage).get mustBe false
              result.get(DateOfBirthPage) mustBe None
              result.get(CountryOfNationalityYesNoPage).get mustBe true
              result.get(CountryOfNationalityUkYesNoPage).get mustBe true
              result.get(CountryOfNationalityPage).get mustBe GB
              result.get(NationalInsuranceNumberYesNoPage).get mustBe false
              result.get(NationalInsuranceNumberPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe true
              result.get(CountryOfResidenceUkYesNoPage).get mustBe true
              result.get(CountryOfResidencePage).get mustBe GB
              result.get(AddressYesNoPage).get mustBe false
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
              result.get(PassportOrIdCardDetailsPage) mustBe None
              result.get(MentalCapacityYesNoPage).get mustBe true
              result.get(StartDatePage).get mustBe startDate
            }
          }

          "non-taxable" when {

            val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false, isUnderlyingData5mld = true)

            "has non-UK nationality, non-UK residency and is legally incapable" in {

              val protector = IndividualProtector(
                name = name,
                dateOfBirth = None,
                countryOfNationality = Some(country),
                identification = None,
                countryOfResidence = Some(country),
                address = None,
                mentalCapacityYesNo = Some(false),
                entityStart = startDate,
                provisional = true
              )

              val result = extractor(baseAnswers, protector, index).get

              result.get(IndexPage).get mustBe index
              result.get(NamePage).get mustBe name
              result.get(DateOfBirthYesNoPage).get mustBe false
              result.get(DateOfBirthPage) mustBe None
              result.get(CountryOfNationalityYesNoPage).get mustBe true
              result.get(CountryOfNationalityUkYesNoPage).get mustBe false
              result.get(CountryOfNationalityPage).get mustBe country
              result.get(NationalInsuranceNumberYesNoPage) mustBe None
              result.get(NationalInsuranceNumberPage) mustBe None
              result.get(CountryOfResidenceYesNoPage).get mustBe true
              result.get(CountryOfResidenceUkYesNoPage).get mustBe false
              result.get(CountryOfResidencePage).get mustBe country
              result.get(AddressYesNoPage) mustBe None
              result.get(LiveInTheUkYesNoPage) mustBe None
              result.get(UkAddressPage) mustBe None
              result.get(NonUkAddressPage) mustBe None
              result.get(PassportOrIdCardDetailsYesNoPage) mustBe None
              result.get(PassportOrIdCardDetailsPage) mustBe None
              result.get(MentalCapacityYesNoPage).get mustBe false
              result.get(StartDatePage).get mustBe startDate
            }
          }
        }
      }
    }
  }
}
