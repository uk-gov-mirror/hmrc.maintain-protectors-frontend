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

import controllers.individual.add.{routes => addRts}
import controllers.individual.amend.{routes => amendRts}
import controllers.individual.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.individual._
import play.api.mvc.Call

import javax.inject.Inject

class IndividualProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => navigateAwayFromDateOfBirthQuestions(mode, _)
    case CountryOfNationalityPage => navigateAwayFromCountryOfNationalityQuestions(mode, _)
    case NationalInsuranceNumberPage => navigateAwayFromNationalInsuranceNumberQuestions(mode, _)
    case PassportDetailsPage | IdCardDetailsPage | PassportOrIdCardDetailsPage => navigateAwayFromIdentificationQuestions(mode, _)
    case MentalCapacityYesNoPage => navigateToStartDateQuestionOrCheckDetails(mode, _)
    case CountryOfResidencePage => navigateAwayFromCountryOfResidenceQuestions(mode, _)
    case UkAddressPage | NonUkAddressPage => _ => navigateAwayFromAddressQuestions(mode)
    case StartDatePage => _ => addRts.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(mode), navigateAwayFromDateOfBirthQuestions(mode, ua))
    case CountryOfNationalityYesNoPage => ua =>
      yesNoNav(ua, CountryOfNationalityYesNoPage, rts.CountryOfNationalityUkYesNoController.onPageLoad(mode), navigateAwayFromCountryOfNationalityQuestions(mode, ua))
    case CountryOfNationalityUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfNationalityUkYesNoPage, navigateAwayFromCountryOfNationalityQuestions(mode, ua), rts.CountryOfNationalityController.onPageLoad(mode))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(mode), navigateAwayFromNationalInsuranceNumberQuestions(mode, ua))
    case CountryOfResidenceYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceYesNoPage, rts.CountryOfResidenceUkYesNoController.onPageLoad(mode), navigateAwayFromCountryOfResidenceQuestions(mode, ua))
    case CountryOfResidenceUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceUkYesNoPage, navigateAwayFromCountryOfResidenceQuestions(mode, ua), rts.CountryOfResidenceController.onPageLoad(mode))
    case AddressYesNoPage => ua =>
      yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), navigateToStartDateQuestionOrCheckDetails(mode, ua))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
    case PassportDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportDetailsYesNoPage, addRts.PassportDetailsController.onPageLoad(), addRts.IdCardDetailsYesNoController.onPageLoad())
    case IdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, IdCardDetailsYesNoPage, addRts.IdCardDetailsController.onPageLoad(), navigateAwayFromIdentificationQuestions(mode, ua))
    case PassportOrIdCardDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportOrIdCardDetailsYesNoPage, amendRts.PassportOrIdCardDetailsController.onPageLoad(), navigateAwayFromIdentificationQuestions(mode, ua))
  }

  private def navigateAwayFromDateOfBirthQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (ua.is5mldEnabled) {
      rts.CountryOfNationalityYesNoController.onPageLoad(mode)
    } else {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromCountryOfNationalityQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (ua.isTaxable) {
      rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    } else {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromNationalInsuranceNumberQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (isNinoDefined(ua)) {
      navigateAwayFromIdentificationQuestions(mode, ua)
    } else {
      if (ua.is5mldEnabled) {
        rts.CountryOfResidenceYesNoController.onPageLoad(mode)
      } else {
        rts.AddressYesNoController.onPageLoad(mode)
      }
    }
  }

  private def navigateAwayFromCountryOfResidenceQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (isNinoDefined(ua) || !ua.isTaxable) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromAddressQuestions(mode: Mode): Call = {
    if (mode == NormalMode) {
      addRts.PassportDetailsYesNoController.onPageLoad()
    } else {
      amendRts.PassportOrIdCardDetailsYesNoController.onPageLoad()
    }
  }

  private def navigateAwayFromIdentificationQuestions(mode: Mode, ua: UserAnswers): Call = {
    if (ua.is5mldEnabled) {
      rts.MentalCapacityYesNoController.onPageLoad(mode)
    } else {
      navigateToStartDateQuestionOrCheckDetails(mode, ua)
    }
  }

  private def navigateToStartDateQuestionOrCheckDetails(mode: Mode, ua: UserAnswers): Call = {
    if (mode == NormalMode) {
      addRts.StartDateController.onPageLoad()
    } else {
      navigateToCheckDetails(ua)
    }
  }

  private def navigateToCheckDetails(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case Some(x) =>
        controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(x)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def isNinoDefined(ua: UserAnswers): Boolean = ua.get(NationalInsuranceNumberPage).isDefined

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode)

}
