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

package models

import models.AddAProtector.{NoComplete, YesNow}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class AddAProtectorSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "AddAProtector" must {

    "only include yes and no as values" in {

      AddAProtector.values mustBe Seq(YesNow, NoComplete)

    }

    "deserialise valid values" in {

      val gen = Gen.oneOf(AddAProtector.values.toSeq)

      forAll(gen) {
        addABeneficiary =>

          JsString(addABeneficiary.toString).validate[AddAProtector].asOpt.value mustEqual addABeneficiary
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!AddAProtector.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[AddAProtector] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(AddAProtector.values.toSeq)

      forAll(gen) {
        addAProtector =>

          Json.toJson(addAProtector) mustEqual JsString(addAProtector.toString)
      }
    }
  }
}
