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

import com.google.inject.Inject
import controllers.individual.add.{routes => addRts}
import controllers.individual.amend.{routes => amendRts}
import controllers.individual.{routes => rts}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.individual._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class IndividualProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, protectorName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, protectorName)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.nameQuestion(NamePage, "individualProtector.name", rts.NameController.onPageLoad(mode).url),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "individualProtector.dateOfBirthYesNo", rts.DateOfBirthYesNoController.onPageLoad(mode).url),
        bound.dateQuestion(DateOfBirthPage, "individualProtector.dateOfBirth", rts.DateOfBirthController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfNationalityYesNoPage, "individualProtector.countryOfNationalityYesNo", rts.CountryOfNationalityYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfNationalityUkYesNoPage, "individualProtector.countryOfNationalityUkYesNo", rts.CountryOfNationalityUkYesNoController.onPageLoad(mode).url),
        bound.countryQuestion(CountryOfNationalityUkYesNoPage, CountryOfNationalityPage, "individualProtector.countryOfNationality", rts.CountryOfNationalityController.onPageLoad(mode).url),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "individualProtector.nationalInsuranceNumberYesNo", rts.NationalInsuranceNumberYesNoController.onPageLoad(mode).url),
        bound.ninoQuestion(NationalInsuranceNumberPage, "individualProtector.nationalInsuranceNumber", rts.NationalInsuranceNumberController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceYesNoPage, "individualProtector.countryOfResidenceYesNo", rts.CountryOfResidenceYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(CountryOfResidenceUkYesNoPage, "individualProtector.countryOfResidenceUkYesNo", rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url),
        bound.countryQuestion(CountryOfResidenceUkYesNoPage, CountryOfResidencePage, "individualProtector.countryOfResidence", rts.CountryOfResidenceController.onPageLoad(mode).url),
        bound.yesNoQuestion(AddressYesNoPage, "individualProtector.addressYesNo", rts.AddressYesNoController.onPageLoad(mode).url),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "individualProtector.liveInTheUkYesNo", rts.LiveInTheUkYesNoController.onPageLoad(mode).url),
        bound.addressQuestion(UkAddressPage, "individualProtector.ukAddress", rts.UkAddressController.onPageLoad(mode).url),
        bound.addressQuestion(NonUkAddressPage, "individualProtector.nonUkAddress", rts.NonUkAddressController.onPageLoad(mode).url),
        if (mode == NormalMode) bound.yesNoQuestion(PassportDetailsYesNoPage, "individualProtector.passportDetailsYesNo", addRts.PassportDetailsYesNoController.onPageLoad().url) else None,
        if (mode == NormalMode) bound.passportDetailsQuestion(PassportDetailsPage, "individualProtector.passportDetails", addRts.PassportDetailsController.onPageLoad().url) else None,
        if (mode == NormalMode) bound.yesNoQuestion(IdCardDetailsYesNoPage, "individualProtector.idCardDetailsYesNo", addRts.IdCardDetailsYesNoController.onPageLoad().url) else None,
        if (mode == NormalMode) bound.idCardDetailsQuestion(IdCardDetailsPage, "individualProtector.idCardDetails", addRts.IdCardDetailsController.onPageLoad().url) else None,
        if (mode == CheckMode) bound.yesNoQuestion(PassportOrIdCardDetailsYesNoPage, "individualProtector.passportOrIdCardDetailsYesNo", amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad().url) else None,
        if (mode == CheckMode) bound.passportOrIdCardDetailsQuestion(PassportOrIdCardDetailsPage, "individualProtector.passportOrIdCardDetails", amendRts.PassportOrIdCardDetailsController.onPageLoad().url) else None,
        bound.yesNoQuestion(MentalCapacityYesNoPage, "individualProtector.mentalCapacityYesNo", rts.MentalCapacityYesNoController.onPageLoad(mode).url),
        if (mode == NormalMode) bound.dateQuestion(StartDatePage, "individualProtector.startDate", addRts.StartDateController.onPageLoad().url) else None
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)
  }
}
