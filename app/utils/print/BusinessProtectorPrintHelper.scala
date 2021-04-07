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

import controllers.business.{routes => rts}
import com.google.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.business._
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

class BusinessProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, protectorName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, protectorName)

    def answerRows(mode: Mode): Seq[Option[AnswerRow]] = Seq(
      bound.stringQuestion(NamePage, "businessProtector.name", rts.NameController.onPageLoad(mode).url),
      bound.yesNoQuestion(UtrYesNoPage, "businessProtector.utrYesNo", rts.UtrYesNoController.onPageLoad(mode).url),
      bound.stringQuestion(UtrPage, "businessProtector.utr", rts.UtrController.onPageLoad(mode).url),
      bound.yesNoQuestion(CountryOfResidenceYesNoPage, "businessProtector.countryOfResidenceYesNo", rts.CountryOfResidenceYesNoController.onPageLoad(mode).url),
      bound.yesNoQuestion(CountryOfResidenceUkYesNoPage, "businessProtector.countryOfResidenceUkYesNo", rts.CountryOfResidenceUkYesNoController.onPageLoad(mode).url),
      bound.countryQuestion(CountryOfResidenceUkYesNoPage, CountryOfResidencePage, "businessProtector.countryOfResidence", rts.CountryOfResidenceController.onPageLoad(mode).url),
      bound.yesNoQuestion(AddressYesNoPage, "businessProtector.addressYesNo", rts.AddressYesNoController.onPageLoad(mode).url),
      bound.yesNoQuestion(AddressUkYesNoPage, "businessProtector.addressUkYesNo", rts.AddressUkYesNoController.onPageLoad(mode).url),
      bound.addressQuestion(UkAddressPage, "businessProtector.ukAddress", rts.UkAddressController.onPageLoad(mode).url),
      bound.addressQuestion(NonUkAddressPage, "businessProtector.nonUkAddress", rts.NonUkAddressController.onPageLoad(mode).url)
    )

    lazy val add: Seq[AnswerRow] = (
      answerRows(NormalMode) :+
        bound.dateQuestion(StartDatePage, "businessProtector.startDate", rts.StartDateController.onPageLoad().url)
      ).flatten

    val amend: Seq[AnswerRow] = answerRows(CheckMode).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
