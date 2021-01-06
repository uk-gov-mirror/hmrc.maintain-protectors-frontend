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

package models

import play.api.libs.json.{JsString, Writes}
import viewmodels.RadioOption

sealed trait ProtectorType

object ProtectorType extends Enumerable.Implicits {

  case object IndividualProtector extends WithName("individual") with ProtectorType
  case object BusinessProtector extends WithName("business") with ProtectorType

  val values: List[ProtectorType] = List(
    IndividualProtector, BusinessProtector
  )

  val options: List[RadioOption] = values.map {
    value =>
      RadioOption("whatTypeOfProtector", value.toString)
  }

  implicit val enumerable: Enumerable[ProtectorType] =
    Enumerable(values.map(v => v.toString -> v): _*)

  val writesToTrusts : Writes[ProtectorType] = Writes {
    case IndividualProtector => JsString("protector")
    case BusinessProtector => JsString("protectorCompany")
  }
}