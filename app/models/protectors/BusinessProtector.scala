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

import models.Address
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class BusinessProtector(name: String,
                                   utr: Option[String],
                                   address: Option[Address],
                                   entityStart: LocalDate,
                                   provisional : Boolean) extends Protector

object BusinessProtector extends ProtectorReads {

  implicit val reads: Reads[BusinessProtector] = (
    (__ \ 'name).read[String] and
      __.lazyRead(readNullableAtSubPath[String](__ \ 'identification \ 'utr)) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'identification \ 'address)) and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").readWithDefault(false)
    )(BusinessProtector.apply _)

  implicit val writes: Writes[BusinessProtector] = (
    (__ \ 'name).write[String] and
      (__ \ 'identification \ 'utr).writeNullable[String] and
      (__ \ 'identification \ 'address).writeNullable[Address] and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
    )(unlift(BusinessProtector.unapply))
}