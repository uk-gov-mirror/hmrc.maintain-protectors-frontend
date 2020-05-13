/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{CheckMode, NormalMode, UserAnswers}
import pages.individual._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class IndividualProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                               countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, protectorName: String)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, protectorName, countryOptions)

    val add: Seq[AnswerRow] = Seq(
        bound.nameQuestion(NamePage, "individualProtector.name", controllers.individual.routes.NameController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(DateOfBirthYesNoPage, "individualProtector.dateOfBirthYesNo", controllers.individual.routes.DateOfBirthYesNoController.onPageLoad(NormalMode).url),
        bound.dateQuestion(DateOfBirthPage, "individualProtector.dateOfBirth", controllers.individual.routes.DateOfBirthController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "individualProtector.nationalInsuranceNumberYesNo", controllers.individual.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url),
        bound.ninoQuestion(NationalInsuranceNumberPage, "individualProtector.nationalInsuranceNumber", controllers.individual.routes.NationalInsuranceNumberController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(AddressYesNoPage, "individualProtector.addressYesNo", controllers.individual.routes.AddressYesNoController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(LiveInTheUkYesNoPage, "individualProtector.liveInTheUkYesNo", controllers.individual.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode).url),
        bound.addressQuestion(UkAddressPage, "individualProtector.ukAddress", controllers.individual.routes.UkAddressController.onPageLoad(NormalMode).url),
        bound.addressQuestion(NonUkAddressPage, "individualProtector.nonUkAddress",controllers.individual.routes.NonUkAddressController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(PassportDetailsYesNoPage, "individualProtector.passportDetailsYesNo", controllers.individual.routes.PassportDetailsYesNoController.onPageLoad(NormalMode).url),
        bound.passportDetailsQuestion(PassportDetailsPage, "individualProtector.passportDetails", controllers.individual.routes.PassportDetailsController.onPageLoad(NormalMode).url),
        bound.yesNoQuestion(IdCardDetailsYesNoPage, "individualProtector.idCardDetailsYesNo", controllers.individual.routes.IdCardDetailsYesNoController.onPageLoad(NormalMode).url),
        bound.idCardDetailsQuestion(IdCardDetailsPage, "individualProtector.idCardDetails", controllers.individual.routes.IdCardDetailsController.onPageLoad(NormalMode).url),
        bound.dateQuestion(StartDatePage, "individualProtector.startDate", controllers.individual.routes.StartDateController.onPageLoad().url)
      ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "individualProtector.name", controllers.individual.routes.NameController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "individualProtector.dateOfBirthYesNo", controllers.individual.routes.DateOfBirthYesNoController.onPageLoad(CheckMode).url),
      bound.dateQuestion(DateOfBirthPage, "individualProtector.dateOfBirth", controllers.individual.routes.DateOfBirthController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "individualProtector.nationalInsuranceNumberYesNo", controllers.individual.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url),
      bound.ninoQuestion(NationalInsuranceNumberPage, "individualProtector.nationalInsuranceNumber", controllers.individual.routes.NationalInsuranceNumberController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(AddressYesNoPage, "individualProtector.addressYesNo", controllers.individual.routes.AddressYesNoController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "individualProtector.liveInTheUkYesNo", controllers.individual.routes.LiveInTheUkYesNoController.onPageLoad(CheckMode).url),
      bound.addressQuestion(UkAddressPage, "individualProtector.ukAddress", controllers.individual.routes.UkAddressController.onPageLoad(CheckMode).url),
      bound.addressQuestion(NonUkAddressPage, "individualProtector.nonUkAddress", controllers.individual.routes.NonUkAddressController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(PassportDetailsYesNoPage, "individualProtector.passportDetailsYesNo", controllers.individual.routes.PassportDetailsYesNoController.onPageLoad(CheckMode).url),
      bound.passportDetailsQuestion(PassportDetailsPage, "individualProtector.passportDetails", controllers.individual.routes.PassportDetailsController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(IdCardDetailsYesNoPage, "individualProtector.idCardDetailsYesNo", controllers.individual.routes.IdCardDetailsYesNoController.onPageLoad(CheckMode).url),
      bound.idCardDetailsQuestion(IdCardDetailsPage, "individualProtector.idCardDetails", controllers.individual.routes.IdCardDetailsController.onPageLoad(CheckMode).url)
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
