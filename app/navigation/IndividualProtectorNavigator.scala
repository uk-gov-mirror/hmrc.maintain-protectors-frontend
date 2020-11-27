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

import controllers.individual.add.{routes => addRts}
import controllers.individual.{routes => rts}
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.individual._
import pages.{Page, QuestionPage}
import play.api.mvc.Call

class IndividualProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  private def simpleNavigation(mode: Mode): PartialFunction[Page, Call] = {
    case NamePage => rts.DateOfBirthYesNoController.onPageLoad(mode)
    case DateOfBirthPage => rts.NationalInsuranceNumberYesNoController.onPageLoad(mode)
    case UkAddressPage => addRts.PassportDetailsYesNoController.onPageLoad()
    case NonUkAddressPage => addRts.PassportDetailsYesNoController.onPageLoad()
    case StartDatePage => addRts.CheckDetailsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case DateOfBirthYesNoPage => ua =>
      yesNoNav(ua, DateOfBirthYesNoPage, rts.DateOfBirthController.onPageLoad(mode), rts.NationalInsuranceNumberYesNoController.onPageLoad(mode))
    case NationalInsuranceNumberYesNoPage => ua =>
      yesNoNav(ua, NationalInsuranceNumberYesNoPage, rts.NationalInsuranceNumberController.onPageLoad(mode), rts.AddressYesNoController.onPageLoad(mode))
    case LiveInTheUkYesNoPage => ua =>
      yesNoNav(ua, LiveInTheUkYesNoPage, rts.UkAddressController.onPageLoad(mode), rts.NonUkAddressController.onPageLoad(mode))
    case PassportDetailsYesNoPage => ua =>
      yesNoNav(ua, PassportDetailsYesNoPage, addRts.PassportDetailsController.onPageLoad(), addRts.IdCardDetailsYesNoController.onPageLoad())
  }

  private def navigationWithCheck(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    mode match {
      case NormalMode => {
        case NationalInsuranceNumberPage | PassportDetailsPage | IdCardDetailsPage  => _ =>
          addRts.StartDateController.onPageLoad()
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), addRts.StartDateController.onPageLoad())
        case IdCardDetailsYesNoPage => ua =>
          yesNoNav(ua, IdCardDetailsYesNoPage, addRts.IdCardDetailsController.onPageLoad(), addRts.StartDateController.onPageLoad())
      }
      case CheckMode => {
        case NationalInsuranceNumberPage | PassportDetailsPage | IdCardDetailsPage => ua =>
          checkDetailsRoute(ua)
        case AddressYesNoPage => ua =>
          yesNoNav(ua, AddressYesNoPage, rts.LiveInTheUkYesNoController.onPageLoad(mode), checkDetailsRoute(ua))
        case IdCardDetailsYesNoPage => ua =>
          yesNoNav(ua, IdCardDetailsYesNoPage, addRts.IdCardDetailsController.onPageLoad(), checkDetailsRoute(ua))
      }
    }
  }

  def yesNoNav(ua: UserAnswers, fromPage: QuestionPage[Boolean], yesCall: => Call, noCall: => Call): Call = {
    ua.get(fromPage)
      .map(if (_) yesCall else noCall)
      .getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
  }

  def checkDetailsRoute(answers: UserAnswers): Call = {
    answers.get(IndexPage) match {
      case None => controllers.routes.SessionExpiredController.onPageLoad()
      case Some(x) =>
        controllers.individual.amend.routes.CheckDetailsController.renderFromUserAnswers(x)
    }
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) andThen (c => (_: UserAnswers) => c) orElse
      yesNoNavigation(mode)  orElse
      navigationWithCheck(mode)

}

