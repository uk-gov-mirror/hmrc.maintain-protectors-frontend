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

import controllers.business.{routes => rts}
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.business._
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class BusinessProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, Call] = {
    case NamePage => rts.UtrYesNoController.onPageLoad(mode)
    case StartDatePage => controllers.business.add.routes.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case UtrYesNoPage => ua =>
      yesNoNav(ua, UtrYesNoPage, rts.UtrController.onPageLoad(mode), rts.AddressYesNoController.onPageLoad(mode))
    case AddressUkYesNoPage => ua =>
      yesNoNav(ua, AddressUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
  }

  private def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def navigationWithCheck(mode: Mode) : PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case UtrPage | UkAddressPage | NonUkAddressPage => _ =>
          rts.StartDateController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.AddressUkYesNoController.onPageLoad(mode), yesNoNav(ua, AddressYesNoPage, rts.AddressUkYesNoController.onPageLoad(mode), rts.StartDateController.onPageLoad()))
      }
      case CheckMode => {
        case UtrPage | UkAddressPage | NonUkAddressPage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.AddressUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
      }
    }
  }

  private def checkDetailsRoute(answers: UserAnswers) : Call = {
    answers.get(IndexPage) match {
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
      case Some(_) =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
    }
  }

  private def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) andThen (c => (_: UserAnswers) => c) orElse
      yesNoNavigation(mode) orElse
      navigationWithCheck(mode)
}

