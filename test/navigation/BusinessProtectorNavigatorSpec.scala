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

package navigation

import base.SpecBase
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.business._

class BusinessProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new BusinessProtectorNavigator

  "Business protector navigator" when {

    "add journey navigation" must {

      val mode = NormalMode

      "Name page -> Do you know UTR page" in {
        navigator.nextPage(NamePage, mode, emptyUserAnswers)
          .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
      }

      "Do you know UTR page -> Yes -> UTR page" in {
        val answers = emptyUserAnswers
          .set(UtrYesNoPage, true).success.value

        navigator.nextPage(UtrYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
      }

      "UTR page -> Start date page" in {
        navigator.nextPage(UtrPage, mode, emptyUserAnswers)
          .mustBe(controllers.business.routes.StartDateController.onPageLoad())
      }

      "Do you know UTR page -> No -> Do you know address page" in {
        val answers = emptyUserAnswers
          .set(UtrYesNoPage, false).success.value

        navigator.nextPage(UtrYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.AddressUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Start date page" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.StartDateController.onPageLoad())
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage, true).success.value

        navigator.nextPage(AddressUkYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage, false).success.value

        navigator.nextPage(AddressUkYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
      }

      "UK address page -> Start date page" in {
        navigator.nextPage(UkAddressPage, mode, emptyUserAnswers)
          .mustBe(controllers.business.routes.StartDateController.onPageLoad())
      }

      "Non-UK address page -> Start date page" in {
        navigator.nextPage(NonUkAddressPage, mode, emptyUserAnswers)
          .mustBe(controllers.business.routes.StartDateController.onPageLoad())
      }

      "Start date page -> Check details page" in {
        navigator.nextPage(StartDatePage, mode, emptyUserAnswers)
          .mustBe(controllers.business.add.routes.CheckDetailsController.onPageLoad())
      }
    }

    "amend journey navigation" must {

      val index = 0
      val mode = CheckMode

      val baseAnswers = emptyUserAnswers
        .set(IndexPage, index).success.value

      "Name page -> Do you know UTR page" in {
        navigator.nextPage(NamePage, mode, baseAnswers)
          .mustBe(controllers.business.routes.UtrYesNoController.onPageLoad(mode))
      }

      "Do you know UTR page -> Yes -> UTR page" in {
        val answers = baseAnswers
          .set(UtrYesNoPage, true).success.value

        navigator.nextPage(UtrYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.UtrController.onPageLoad(mode))
      }

      "UTR page -> Check details page" in {
        navigator.nextPage(UtrPage, mode, baseAnswers)
          .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Do you know UTR page -> No -> Do you know address page" in {
        val answers = baseAnswers
          .set(UtrYesNoPage, false).success.value

        navigator.nextPage(UtrYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> Yes -> Is address in UK page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, true).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.AddressUkYesNoController.onPageLoad(mode))
      }

      "Do you know address page -> No -> Check details page" in {
        val answers = baseAnswers
          .set(AddressYesNoPage, false).success.value

        navigator.nextPage(AddressYesNoPage, mode, answers)
          .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Is address in UK page -> Yes -> UK address page" in {
        val answers = baseAnswers
          .set(AddressUkYesNoPage, true).success.value

        navigator.nextPage(AddressUkYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.UkAddressController.onPageLoad(mode))
      }

      "Is address in UK page -> No -> Non-UK address page" in {
        val answers = baseAnswers
          .set(AddressUkYesNoPage, false).success.value

        navigator.nextPage(AddressUkYesNoPage, mode, answers)
          .mustBe(controllers.business.routes.NonUkAddressController.onPageLoad(mode))
      }

      "UK address page -> Check details page" in {
        navigator.nextPage(UkAddressPage, mode, baseAnswers)
          .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }

      "Non-UK address page -> Check details page" in {
        navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
          .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
      }
    }
  }
}
