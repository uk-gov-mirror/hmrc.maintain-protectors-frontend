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

import java.time.LocalDate

import base.SpecBase
import models.{CheckMode, NonUkAddress, NormalMode, UkAddress}
import pages.business._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class BusinessProtectorPrintHelperSpec extends SpecBase {

  private val name: String = "Name"
  private val utr: String = "1234567890"
  private val ukAddress: UkAddress = UkAddress("value 1", "value 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("value 1", "value 2", None, "DE")

  "BusinessProtectorPrintHelper" must {

    val userAnswers = emptyUserAnswers
      .set(NamePage, name).success.value
      .set(UtrYesNoPage, true).success.value
      .set(UtrPage, utr).success.value
      .set(AddressYesNoPage, true).success.value
      .set(AddressUkYesNoPage, true).success.value
      .set(UkAddressPage, ukAddress).success.value
      .set(NonUkAddressPage, nonUkAddress).success.value
      .set(StartDatePage, LocalDate.of(2020, 1, 1)).success.value

    val helper = injector.instanceOf[BusinessProtectorPrintHelper]

    "generate add business protector section for all possible data" in {

      val mode = NormalMode

      val result = helper(userAnswers, provisional = true, name)

      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("businessProtector.name.checkYourAnswersLabel")), answer = Html("Name"), changeUrl = controllers.business.routes.NameController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.utrYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.UtrYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.utr.checkYourAnswersLabel", name)), answer = Html("1234567890"), changeUrl = controllers.business.routes.UtrController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.addressYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.AddressYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.addressUkYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.AddressUkYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.ukAddress.checkYourAnswersLabel", name)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = controllers.business.routes.UkAddressController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.nonUkAddress.checkYourAnswersLabel", name)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = controllers.business.routes.NonUkAddressController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.startDate.checkYourAnswersLabel", name)), answer = Html("1 January 2020"), changeUrl = controllers.business.routes.StartDateController.onPageLoad().url)
        )
      )
    }

    "generate amend business protector section for all possible data" in {

      val mode = CheckMode

      val result = helper(userAnswers, provisional = false, name)

      result mustBe AnswerSection(
        headingKey = None,
        rows = Seq(
          AnswerRow(label = Html(messages("businessProtector.name.checkYourAnswersLabel")), answer = Html("Name"), changeUrl = controllers.business.routes.NameController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.utrYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.UtrYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.utr.checkYourAnswersLabel", name)), answer = Html("1234567890"), changeUrl = controllers.business.routes.UtrController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.addressYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.AddressYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.addressUkYesNo.checkYourAnswersLabel", name)), answer = Html("Yes"), changeUrl = controllers.business.routes.AddressUkYesNoController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.ukAddress.checkYourAnswersLabel", name)), answer = Html("value 1<br />value 2<br />AB1 1AB"), changeUrl = controllers.business.routes.UkAddressController.onPageLoad(mode).url),
          AnswerRow(label = Html(messages("businessProtector.nonUkAddress.checkYourAnswersLabel", name)), answer = Html("value 1<br />value 2<br />Germany"), changeUrl = controllers.business.routes.NonUkAddressController.onPageLoad(mode).url)
        )
      )
    }
  }
}
