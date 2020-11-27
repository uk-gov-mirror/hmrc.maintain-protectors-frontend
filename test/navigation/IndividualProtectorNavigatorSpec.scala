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

package navigation
import base.SpecBase
import controllers.individual.add.{routes => addRts}
import controllers.individual.{routes => rts}
import models.{CheckMode, NormalMode}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.individual._

class IndividualProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks  {

  val navigator = new IndividualProtectorNavigator

  "Individual protector navigator" when {

      "Name page -> Do you know date of birth page" in {
        navigator.nextPage(NamePage, NormalMode, emptyUserAnswers)
          .mustBe(rts.DateOfBirthYesNoController.onPageLoad(NormalMode))
      }

    "Do you know date of birth page -> Yes -> Date of birth page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, true).success.value

      navigator.nextPage(DateOfBirthYesNoPage, NormalMode, answers)
        .mustBe(rts.DateOfBirthController.onPageLoad(NormalMode))
    }

    "Date of birth page -> Do you know NINO page" in {
      navigator.nextPage(DateOfBirthPage, NormalMode, emptyUserAnswers)
        .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode))
    }

    "Do you know date of birth page -> No -> Do you know NINO page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, false).success.value

      navigator.nextPage(DateOfBirthYesNoPage, NormalMode, answers)
        .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode))
    }

    "Do you know NINO page -> Yes -> NINO page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, true).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, NormalMode, answers)
        .mustBe(rts.NationalInsuranceNumberController.onPageLoad(NormalMode))
    }

    "NINO page -> Start Date page" in {
      navigator.nextPage(NationalInsuranceNumberPage, NormalMode, emptyUserAnswers)
        .mustBe(addRts.StartDateController.onPageLoad())
    }

    "Do you know NINO page -> No -> Do you know address page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, false).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, NormalMode, answers)
        .mustBe(rts.AddressYesNoController.onPageLoad(NormalMode))
    }

    "Do you know address page -> Yes -> Is address in UK page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, true).success.value

      navigator.nextPage(AddressYesNoPage, NormalMode, answers)
        .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(NormalMode))
    }

    "Do you know address page -> No -> Start Date page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, false).success.value

      navigator.nextPage(AddressYesNoPage, NormalMode, answers)
        .mustBe(addRts.StartDateController.onPageLoad())
    }

    "Is address in UK page -> Yes -> UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, true).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, NormalMode, answers)
        .mustBe(rts.UkAddressController.onPageLoad(NormalMode))
    }

    "UK address page -> Do you know passport details page" in {
      navigator.nextPage(UkAddressPage, NormalMode, emptyUserAnswers)
        .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
    }

    "Is address in UK page -> No -> Non-UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, false).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, NormalMode, answers)
        .mustBe(rts.NonUkAddressController.onPageLoad(NormalMode))
    }

    "Non-UK address page -> Do you know passport details page" in {
      navigator.nextPage(NonUkAddressPage, NormalMode, emptyUserAnswers)
        .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
    }

    "Do you know passport details page -> Yes -> Passport details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, true).success.value

      navigator.nextPage(PassportDetailsYesNoPage, NormalMode, answers)
        .mustBe(addRts.PassportDetailsController.onPageLoad())
    }

    "Passport details page -> Start Date page" in {
      navigator.nextPage(PassportDetailsPage, NormalMode, emptyUserAnswers)
        .mustBe(addRts.StartDateController.onPageLoad())
    }

    "Do you know passport details page -> No -> Do you know ID card details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, false).success.value

      navigator.nextPage(PassportDetailsYesNoPage, NormalMode, answers)
        .mustBe(addRts.IdCardDetailsYesNoController.onPageLoad())
    }

    "Do you know ID card details page -> Yes -> ID card details page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, true).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, NormalMode, answers)
        .mustBe(addRts.IdCardDetailsController.onPageLoad())
    }

    "ID card details page -> Start Date page" in {
      navigator.nextPage(IdCardDetailsPage, NormalMode, emptyUserAnswers)
        .mustBe(addRts.StartDateController.onPageLoad())
    }

    "Do you know ID card details page -> No -> Start Date page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, false).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, NormalMode, answers)
        .mustBe(addRts.StartDateController.onPageLoad())
    }
  }

  "amend journey navigation" must {
    
    val index = 0

    "Name page -> Do you know date of birth page" in {
      navigator.nextPage(NamePage, CheckMode, emptyUserAnswers)
        .mustBe(rts.DateOfBirthYesNoController.onPageLoad(CheckMode))
    }

    "Do you know date of birth page -> Yes -> Date of birth page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, true).success.value

      navigator.nextPage(DateOfBirthYesNoPage, CheckMode, answers)
        .mustBe(rts.DateOfBirthController.onPageLoad(CheckMode))
    }

    "Date of birth page -> Do you know NINO page" in {
      navigator.nextPage(DateOfBirthPage, CheckMode, emptyUserAnswers)
        .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode))
    }

    "Do you know date of birth page -> No -> Do you know NINO page" in {
      val answers = emptyUserAnswers
        .set(DateOfBirthYesNoPage, false).success.value

      navigator.nextPage(DateOfBirthYesNoPage, CheckMode, answers)
        .mustBe(rts.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode))
    }

    "Do you know NINO page -> Yes -> NINO page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, true).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, CheckMode, answers)
        .mustBe(rts.NationalInsuranceNumberController.onPageLoad(CheckMode))
    }

    "NINO page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, index).success.value

      navigator.nextPage(NationalInsuranceNumberPage, CheckMode, answers)
        .mustBe(controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
    }

    "Do you know NINO page -> No -> Do you know address page" in {
      val answers = emptyUserAnswers
        .set(NationalInsuranceNumberYesNoPage, false).success.value

      navigator.nextPage(NationalInsuranceNumberYesNoPage, CheckMode, answers)
        .mustBe(rts.AddressYesNoController.onPageLoad(CheckMode))
    }

    "Do you know address page -> Yes -> Is address in UK page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, true).success.value

      navigator.nextPage(AddressYesNoPage, CheckMode, answers)
        .mustBe(rts.LiveInTheUkYesNoController.onPageLoad(CheckMode))
    }

    "Do you know address page -> No -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage, false).success.value
        .set(IndexPage, index).success.value

      navigator.nextPage(AddressYesNoPage, CheckMode, answers)
        .mustBe(controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
    }

    "Is address in UK page -> Yes -> UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, true).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, CheckMode, answers)
        .mustBe(rts.UkAddressController.onPageLoad(CheckMode))
    }

    "UK address page -> Do you know passport details page" in {
      navigator.nextPage(UkAddressPage, CheckMode, emptyUserAnswers)
        .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
    }

    "Is address in UK page -> No -> Non-UK address page" in {
      val answers = emptyUserAnswers
        .set(LiveInTheUkYesNoPage, false).success.value

      navigator.nextPage(LiveInTheUkYesNoPage, CheckMode, answers)
        .mustBe(rts.NonUkAddressController.onPageLoad(CheckMode))
    }

    "Non-UK address page -> Do you know passport details page" in {
      navigator.nextPage(NonUkAddressPage, CheckMode, emptyUserAnswers)
        .mustBe(addRts.PassportDetailsYesNoController.onPageLoad())
    }

    "Do you know passport details page -> Yes -> Passport details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, true).success.value

      navigator.nextPage(PassportDetailsYesNoPage, CheckMode, answers)
        .mustBe(addRts.PassportDetailsController.onPageLoad())
    }

    "Passport details page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, index).success.value

      navigator.nextPage(PassportDetailsPage, CheckMode, answers)
        .mustBe(controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
    }

    "Do you know passport details page -> No -> Do you know ID card details page" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage, false).success.value

      navigator.nextPage(PassportDetailsYesNoPage, CheckMode, answers)
        .mustBe(addRts.IdCardDetailsYesNoController.onPageLoad())
    }

    "Do you know ID card details page -> Yes -> ID card details page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, true).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, CheckMode, answers)
        .mustBe(addRts.IdCardDetailsController.onPageLoad())
    }

    "ID card details page -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IndexPage, index).success.value

      navigator.nextPage(IdCardDetailsPage, CheckMode, answers)
        .mustBe(controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
    }

    "Do you know ID card details page -> No -> Check Details page" in {
      val answers = emptyUserAnswers
        .set(IdCardDetailsYesNoPage, false).success.value
        .set(IndexPage, index).success.value

      navigator.nextPage(IdCardDetailsYesNoPage, CheckMode, answers)
        .mustBe(controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
    }
}
}
