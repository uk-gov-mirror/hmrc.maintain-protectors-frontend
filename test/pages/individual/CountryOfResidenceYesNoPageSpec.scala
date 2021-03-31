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

package pages.individual

import models.{UkAddress, UserAnswers}
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class CountryOfResidenceYesNoPageSpec extends PageBehaviours {

  "CountryOfResidenceYesNoPage" must {

    beRetrievable[Boolean](CountryOfResidenceYesNoPage)

    beSettable[Boolean](CountryOfResidenceYesNoPage)

    beRemovable[Boolean](CountryOfResidenceYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = UserAnswers("id", "identifier", LocalDate.now)
        .set(CountryOfResidencePage, "FR").success.value
        .set(CountryOfResidenceUkYesNoPage, false).success.value
        .set(CountryOfResidencePage, "FR").success.value
        .set(AddressYesNoPage, true).success.value
        .set(LiveInTheUkYesNoPage, true).success.value
        .set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode")).success.value

      val result = userAnswers.set(CountryOfResidenceYesNoPage, false).success.value

      result.get(CountryOfResidenceUkYesNoPage) mustBe None
      result.get(CountryOfResidencePage) mustBe None
      result.get(AddressYesNoPage) mustBe Some(true)
    }
  }
}
