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
import pages.business._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class BusinessProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                             countryOptions: CountryOptions
                                          ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, protectorName: String)(implicit messages: Messages): AnswerSection = {

    val bound = answerRowConverter.bind(userAnswers, protectorName, countryOptions)

    val add: Seq[AnswerRow] = Seq(
      bound.stringQuestion(NamePage, "businessProtector.name", controllers.business.routes.NameController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(UtrYesNoPage, "businessProtector.utrYesNo", controllers.business.routes.UtrYesNoController.onPageLoad(NormalMode).url),
      bound.stringQuestion(UtrPage, "businessProtector.utr", controllers.business.routes.UtrController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(AddressYesNoPage, "businessProtector.addressYesNo", controllers.business.routes.AddressYesNoController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(AddressUkYesNoPage, "businessProtector.addressUkYesNo", controllers.business.routes.AddressUkYesNoController.onPageLoad(NormalMode).url),
      bound.addressQuestion(UkAddressPage, "businessProtector.ukAddress", controllers.business.routes.UkAddressController.onPageLoad(NormalMode).url),
      bound.addressQuestion(NonUkAddressPage, "businessProtector.nonUkAddress", controllers.business.routes.NonUkAddressController.onPageLoad(NormalMode).url),
      bound.dateQuestion(StartDatePage, "businessProtector.startDate", controllers.business.routes.StartDateController.onPageLoad().url)
    ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.stringQuestion(NamePage, "businessProtector.name", controllers.business.routes.NameController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(UtrYesNoPage, "businessProtector.utrYesNo", controllers.business.routes.UtrYesNoController.onPageLoad(CheckMode).url),
      bound.stringQuestion(UtrPage, "businessProtector.utr", controllers.business.routes.UtrController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(AddressYesNoPage, "businessProtector.addressYesNo", controllers.business.routes.AddressYesNoController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(AddressUkYesNoPage, "businessProtector.addressUkYesNo", controllers.business.routes.AddressUkYesNoController.onPageLoad(CheckMode).url),
      bound.addressQuestion(UkAddressPage, "businessProtector.ukAddress", controllers.business.routes.UkAddressController.onPageLoad(CheckMode).url),
      bound.addressQuestion(NonUkAddressPage, "businessProtector.nonUkAddress", controllers.business.routes.NonUkAddressController.onPageLoad(CheckMode).url)
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )
  }
}
