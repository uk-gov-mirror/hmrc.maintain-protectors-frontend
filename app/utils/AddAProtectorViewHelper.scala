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

package utils

import models.protectors.{BusinessProtector, IndividualProtector, Protectors}
import play.api.i18n.Messages
import viewmodels.addAnother.{AddRow, AddToRows}

class AddAProtectorViewHelper(protectors: Protectors)(implicit messages: Messages) {

  private def individualProtectorRow(protector: IndividualProtector, index: Int): AddRow = {
    AddRow(
      name = protector.name.displayName,
      typeLabel = messages("entities.protector.individual"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.individual.amend.routes.CheckDetailsController.extractAndRender(index).url),
      removeLabel =  messages("site.delete"),
      removeUrl = Some(controllers.individual.remove.routes.RemoveIndividualProtectorController.onPageLoad(index).url)
    )
  }

  private def businessProtectorRow(protector: BusinessProtector, index: Int): AddRow = {
    AddRow(
      name = protector.name,
      typeLabel = messages("entities.protector.business"),
      changeLabel = messages("site.change.details"),
      changeUrl = Some(controllers.business.amend.routes.CheckDetailsController.extractAndRender(index).url),
      removeLabel =  messages("site.delete"),
      removeUrl = Some(controllers.business.remove.routes.RemoveBusinessProtectorController.onPageLoad(index).url)
    )
  }

  def rows: AddToRows = {
    val complete =
      protectors.protector.zipWithIndex.map(x => individualProtectorRow(x._1, x._2)) ++
      protectors.protectorCompany.zipWithIndex.map(x => businessProtectorRow(x._1, x._2))

    AddToRows(Nil, complete)
  }

}
