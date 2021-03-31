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
        val answers = emptyUserAnswers
          .set(UtrYesNoPage, true).success.value

        navigator.nextPage(UtrPage, mode, answers)
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
        val answers = baseAnswers
          .set(UtrYesNoPage, true).success.value

        navigator.nextPage(UtrPage, mode, answers)
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

    "5mld taxable" must {

      "add journey navigation" must {
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)
        val mode = NormalMode

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

        "UTR page -> Do you know the country of residence" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, true).success.value

          navigator.nextPage(UtrPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> No -> Do you country of residence" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No -> With Utr -> Start Date Page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Do you know the country of residence -> No -> With No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page -> Yes -> With Utr -> Start Date page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }
        
        "Is residence in UK page -> Yes -> With No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value
            .set(UtrYesNoPage, false).success.value
          
          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }
        

        "Country of Residence page -> With Utr -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Do you know address page -> Yes -> Is address in UK page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, true).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressUkYesNoController.onPageLoad(mode))
        }

        "Do you know address page -> No -> Start date page" in {
          val answers = baseAnswers
            .set(AddressYesNoPage, false).success.value

          navigator.nextPage(AddressYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
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

        "UK address page -> Start date page" in {
          navigator.nextPage(UkAddressPage, mode, baseAnswers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Non-UK address page -> Start date page" in {
          navigator.nextPage(NonUkAddressPage, mode, baseAnswers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Start date page -> Check details page" in {
          navigator.nextPage(StartDatePage, mode, baseAnswers)
            .mustBe(controllers.business.add.routes.CheckDetailsController.onPageLoad())
        }
      }

      "amend journey navigation" must {
        val index = 0
        val mode = CheckMode
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)
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

        "UTR page -> Country of Residence page" in {
          navigator.nextPage(UtrPage, mode, baseAnswers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know UTR page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(UtrYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No -> With Utr -> Check Details page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Do you know the country of residence -> No -> With No Utr -> Address page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page-> Yes -> With Utr -> Check details page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Is residence in UK page-> Yes -> With No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> No Utr -> Do you know address page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.routes.AddressYesNoController.onPageLoad(mode))
        }

        "Country of Residence page -> With Utr -> Check details page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
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
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(UkAddressPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Non-UK address page -> Check details page" in {
          val answers = baseAnswers
            .set(IndexPage, index).success.value

          navigator.nextPage(NonUkAddressPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }
      }
    }

    "5mld none taxable" must {

      "add journey navigation" must {
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false)
        val mode = NormalMode

        "Name page -> Do you country of residence" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Is residence in UK page -> Yes -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> Start date page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.routes.StartDateController.onPageLoad())
        }

        "Start date page -> Check details page" in {
          navigator.nextPage(StartDatePage, mode, baseAnswers)
            .mustBe(controllers.business.add.routes.CheckDetailsController.onPageLoad())
        }
      }

      "amend journey navigation" must {
        val index = 0
        val mode = CheckMode
        val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false)
          .set(IndexPage, index).success.value

        "Name page -> Do you country of residence" in {
          navigator.nextPage(NamePage, mode, baseAnswers)
            .mustBe(controllers.business.routes.CountryOfResidenceYesNoController.onPageLoad(mode))
        }


        "Do you know the country of residence -> Yes -> Is residence in UK page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceUkYesNoController.onPageLoad(mode))
        }

        "Do you know the country of residence -> No -> Check Details page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceYesNoPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Is residence in UK page-> Yes -> Check Details page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, true).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }

        "Is residence in UK page -> No -> Country of Residence page" in {
          val answers = baseAnswers
            .set(CountryOfResidenceUkYesNoPage, false).success.value

          navigator.nextPage(CountryOfResidenceUkYesNoPage, mode, answers)
            .mustBe(controllers.business.routes.CountryOfResidenceController.onPageLoad(mode))
        }

        "Country of Residence page -> Check Details page" in {
          val answers = baseAnswers
            .set(CountryOfResidencePage, "ES").success.value
            .set(UtrYesNoPage, true).success.value
            .set(UtrPage, "12345678").success.value

          navigator.nextPage(CountryOfResidencePage, mode, answers)
            .mustBe(controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index))
        }
      }
    }
  }
}
