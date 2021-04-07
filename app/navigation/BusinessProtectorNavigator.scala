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

import controllers.business.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.business._
import play.api.mvc.Call

import javax.inject.Inject

class BusinessProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => ua => navigateAwayFromNamePage(mode, ua)
    case UtrPage => ua => navigateAwayFromUtrPages(mode, ua)
    case CountryOfResidencePage => ua => navigateAwayFromResidencePages(mode, ua)
    case UkAddressPage | NonUkAddressPage => ua => navigateToStartDateOrCheckDetails(mode, ua)
    case StartDatePage => _ => controllers.business.add.routes.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case UtrYesNoPage => ua =>
      yesNoNav(ua, UtrYesNoPage, rts.UtrController.onPageLoad(mode), navigateAwayFromUtrPages(mode, ua))
    case CountryOfResidenceYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceYesNoPage, rts.CountryOfResidenceUkYesNoController.onPageLoad(mode), navigateAwayFromResidencePages(mode, ua))
    case CountryOfResidenceUkYesNoPage => ua =>
      yesNoNav(ua, CountryOfResidenceUkYesNoPage, navigateAwayFromResidencePages(mode, ua), rts.CountryOfResidenceController.onPageLoad(mode))
    case AddressYesNoPage => ua =>
      yesNoNav(ua, AddressYesNoPage, rts.AddressUkYesNoController.onPageLoad(mode), navigateToStartDateOrCheckDetails(mode, ua))
    case AddressUkYesNoPage => ua =>
      yesNoNav(ua, AddressUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
  }

  private def navigateAwayFromNamePage(mode: Mode, answers: UserAnswers): Call = {
    if (answers.is5mldEnabled && !answers.isTaxable) {
      rts.CountryOfResidenceYesNoController.onPageLoad(mode)
    } else {
      rts.UtrYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromUtrPages(mode: Mode, answers: UserAnswers): Call = {
    (answers.is5mldEnabled, isUtrDefined(answers)) match {
      case (true, _) => rts.CountryOfResidenceYesNoController.onPageLoad(mode)
      case (false, true) => navigateToStartDateOrCheckDetails(mode, answers)
      case (false, _) => rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateAwayFromResidencePages(mode: Mode, answers: UserAnswers): Call = {
    val isNonTaxable5mld = answers.is5mldEnabled && !answers.isTaxable

    if (isNonTaxable5mld || isUtrDefined(answers)){
      navigateToStartDateOrCheckDetails(mode, answers)
    } else {
      rts.AddressYesNoController.onPageLoad(mode)
    }
  }

  private def navigateToStartDateOrCheckDetails(mode: Mode, answers: UserAnswers) = {
    if (mode == NormalMode) {
      rts.StartDateController.onPageLoad()
    } else {
      checkDetailsRoute(answers)
    }
  }

  private def isUtrDefined(answers: UserAnswers): Boolean = answers.get(UtrYesNoPage).getOrElse(false)

  private def checkDetailsRoute(answers: UserAnswers) : Call = {
    answers.get(IndexPage) match {
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
      case Some(index) =>
        controllers.business.amend.routes.CheckDetailsController.renderFromUserAnswers(index)
    }
  }

  private def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode)
}

