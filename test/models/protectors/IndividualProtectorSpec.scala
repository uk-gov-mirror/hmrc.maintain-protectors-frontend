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

package models.protectors

import base.SpecBase
import models.Constant.GB
import models.Name
import play.api.libs.json.Json

import java.time.LocalDate

class IndividualProtectorSpec extends SpecBase {

  private val firstName = "Joe"
  private val lastName = "Bloggs"
  private val name = Name(firstName, None, lastName)
  private val date = "2000-01-01"

  "Individual Protector" must {

    "deserialise and serialise" when {

      "legally incapable" in {

        val json = Json.parse(
          s"""
            |{
            |  "name": {
            |    "firstName": "$firstName",
            |    "lastName": "$lastName"
            |  },
            |  "nationality": "$GB",
            |  "countryOfResidence": "$GB",
            |  "legallyIncapable": true,
            |  "entityStart": "$date",
            |  "provisional": false
            |}
            |""".stripMargin
        )

        val deserialised = json.as[IndividualProtector]

        deserialised mustEqual IndividualProtector(
          name = name,
          dateOfBirth = None,
          countryOfNationality = Some(GB),
          identification = None,
          countryOfResidence = Some(GB),
          address = None,
          mentalCapacityYesNo = Some(false),
          entityStart = LocalDate.parse(date),
          provisional = false
        )

        val serialised = Json.toJson(deserialised)

        serialised mustEqual json
      }

      "legally capable" in {

        val json = Json.parse(
          s"""
             |{
             |  "name": {
             |    "firstName": "$firstName",
             |    "lastName": "$lastName"
             |  },
             |  "nationality": "$GB",
             |  "countryOfResidence": "$GB",
             |  "legallyIncapable": false,
             |  "entityStart": "$date",
             |  "provisional": false
             |}
             |""".stripMargin
        )

        val deserialised = json.as[IndividualProtector]

        deserialised mustBe IndividualProtector(
          name = name,
          dateOfBirth = None,
          countryOfNationality = Some(GB),
          identification = None,
          countryOfResidence = Some(GB),
          address = None,
          mentalCapacityYesNo = Some(true),
          entityStart = LocalDate.parse(date),
          provisional = false
        )

        val serialised = Json.toJson(deserialised)

        serialised mustEqual json
      }
    }
  }
}
