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

package utils.print

import java.time.LocalDate

import base.SpecBase
import controllers.individual.add.{routes => addRts}
import controllers.individual.amend.{routes => amendRts}
import controllers.individual.{routes => rts}
import models.{CheckMode, CombinedPassportOrIdCard, IdCard, Mode, Name, NonUkAddress, NormalMode, Passport, UkAddress, UserAnswers}
import pages.individual._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class IndividualProtectorPrintHelperSpec extends SpecBase {

  private val name: Name = Name("First", Some("Middle"), "Last")
  private val ukAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  private val helper: IndividualProtectorPrintHelper = injector.instanceOf[IndividualProtectorPrintHelper]

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(DateOfBirthYesNoPage, true).success.value
    .set(DateOfBirthPage, LocalDate.of(2010, 10, 10)).success.value
    .set(NationalInsuranceNumberYesNoPage, true).success.value
    .set(NationalInsuranceNumberPage, "AA000000A").success.value
    .set(AddressYesNoPage, true).success.value
    .set(LiveInTheUkYesNoPage, true).success.value
    .set(UkAddressPage, ukAddress).success.value
    .set(NonUkAddressPage, nonUkAddress).success.value
    .set(StartDatePage, LocalDate.of(2020, 1, 1)).success.value

  "IndividualProtectorPrintHelper" must {

    "generate individual protector section for all possible data" when {

      "adding" in {

        val mode: Mode = NormalMode

        val userAnswers = baseAnswers
          .set(PassportDetailsYesNoPage, true).success.value
          .set(PassportDetailsPage, Passport("GB", "1", LocalDate.of(2030, 10, 10))).success.value
          .set(IdCardDetailsYesNoPage, true).success.value
          .set(IdCardDetailsPage, IdCard("GB", "1", LocalDate.of(2030, 10, 10))).success.value

        val result = helper(userAnswers, provisional = true, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = Html(messages("individualProtector.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = rts.NameController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.DateOfBirthYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = rts.DateOfBirthController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = rts.NationalInsuranceNumberController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.AddressYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.LiveInTheUkYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = rts.UkAddressController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = rts.NonUkAddressController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.passportDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = addRts.PassportDetailsYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("individualProtector.passportDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = addRts.PassportDetailsController.onPageLoad().url),
            AnswerRow(label = Html(messages("individualProtector.idCardDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = addRts.IdCardDetailsYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("individualProtector.idCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = addRts.IdCardDetailsController.onPageLoad().url),
            AnswerRow(label = Html(messages("individualProtector.startDate.checkYourAnswersLabel", name.displayName)), answer = Html("1 January 2020"), changeUrl = addRts.StartDateController.onPageLoad().url)
          )
        )
      }

      "amending" in {

        val mode: Mode = CheckMode

        val userAnswers = baseAnswers
          .set(PassportOrIdCardDetailsYesNoPage, true).success.value
          .set(PassportOrIdCardDetailsPage, CombinedPassportOrIdCard("GB", "1", LocalDate.of(2030, 10, 10))).success.value

        val result = helper(userAnswers, provisional = false, name.displayName)
        result mustBe AnswerSection(
          headingKey = None,
          rows = Seq(
            AnswerRow(label = Html(messages("individualProtector.name.checkYourAnswersLabel")), answer = Html("First Middle Last"), changeUrl = rts.NameController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.dateOfBirthYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.DateOfBirthYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.dateOfBirth.checkYourAnswersLabel", name.displayName)), answer = Html("10 October 2010"), changeUrl = rts.DateOfBirthController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nationalInsuranceNumberYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nationalInsuranceNumber.checkYourAnswersLabel", name.displayName)), answer = Html("AA 00 00 00 A"), changeUrl = rts.NationalInsuranceNumberController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.addressYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.AddressYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.liveInTheUkYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = rts.LiveInTheUkYesNoController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.ukAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = rts.UkAddressController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.nonUkAddress.checkYourAnswersLabel", name.displayName)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = rts.NonUkAddressController.onPageLoad(mode).url),
            AnswerRow(label = Html(messages("individualProtector.passportOrIdCardDetailsYesNo.checkYourAnswersLabel", name.displayName)), answer = Html("Yes"), changeUrl = amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad().url),
            AnswerRow(label = Html(messages("individualProtector.passportOrIdCardDetails.checkYourAnswersLabel", name.displayName)), answer = Html("United Kingdom<br />1<br />10 October 2030"), changeUrl = amendRts.PassportOrIdCardDetailsController.onPageLoad().url)
          )
        )
      }
    }
  }
}
