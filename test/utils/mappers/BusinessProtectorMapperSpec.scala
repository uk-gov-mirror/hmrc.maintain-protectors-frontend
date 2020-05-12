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

package utils.mappers

import java.time.LocalDate

import base.SpecBase
import models.{NonUkAddress, UkAddress}
import pages.business._

class BusinessProtectorMapperSpec extends SpecBase {

  private val name = "Name"
  private val utr = "1234567890"
  private val startDate = LocalDate.parse("2019-03-09")
  private val ukAddress = UkAddress("line1", "line2", Some("line3"), Some("line4"), "POSTCODE")
  private val nonUkAddress = NonUkAddress("line1", "line2", Some("line3"), "country")

  "BusinessSettlorMapper" when {

    val mapper = injector.instanceOf[BusinessProtectorMapper]

    "generate business protector model with no utr and no address" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(UtrYesNoPage, false).success.value
        .set(AddressYesNoPage, false).success.value
        .set(StartDatePage, startDate).success.value

      val result = mapper(userAnswers).get

      result.name mustBe name
      result.utr mustBe None
      result.address mustBe None
      result.entityStart mustBe startDate
    }

    "generate business protector model with utr and no address" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(UtrYesNoPage, true).success.value
        .set(UtrPage, utr).success.value
        .set(StartDatePage, startDate).success.value

      val result = mapper(userAnswers).get

      result.name mustBe name
      result.utr mustBe Some(utr)
      result.address mustBe None
      result.entityStart mustBe startDate
    }

    "generate business protector model with UK address and no utr" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(UtrYesNoPage, false).success.value
        .set(AddressYesNoPage, true).success.value
        .set(AddressUkYesNoPage, true).success.value
        .set(UkAddressPage, ukAddress).success.value
        .set(StartDatePage, startDate).success.value

      val result = mapper(userAnswers).get

      result.name mustBe name
      result.utr mustBe None
      result.address mustBe Some(ukAddress)
      result.entityStart mustBe startDate
    }

    "generate business protector model with non-UK address and no utr" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage, name).success.value
        .set(UtrYesNoPage, false).success.value
        .set(AddressYesNoPage, true).success.value
        .set(AddressUkYesNoPage, false).success.value
        .set(NonUkAddressPage, nonUkAddress).success.value
        .set(StartDatePage, startDate).success.value

      val result = mapper(userAnswers).get

      result.name mustBe name
      result.utr mustBe None
      result.address mustBe Some(nonUkAddress)
      result.entityStart mustBe startDate
    }
  }
}
