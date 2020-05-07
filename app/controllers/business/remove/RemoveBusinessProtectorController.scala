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

package controllers.business.remove

import controllers.actions.StandardActionSets
import forms.RemoveIndexFormProvider
import javax.inject.Inject
import models.{ProtectorType, RemoveProtector}
import pages.business.RemoveYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.business.remove.RemoveBusinessProtectorView

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ExecutionContext, Future}

class RemoveBusinessProtectorController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    repository: PlaybackRepository,
                                                    standardActionSets: StandardActionSets,
                                                    trustService: TrustService,
                                                    formProvider: RemoveIndexFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: RemoveBusinessProtectorView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val messagesPrefix: String = "removeBusinessProtector"

  private val form = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      trustService.getBusinessProtector(request.userAnswers.utr, index).map {
        protector =>
          Ok(view(preparedForm, index, protector.name))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getBusinessProtector(request.userAnswers.utr, index).map {
            protector =>
              BadRequest(view(formWithErrors, index, protector.name))
          }
        },
        value => {

          if (value) {

            trustService.getBusinessProtector(request.userAnswers.utr, index).flatMap {
              protector =>
                if (protector.provisional) {
                  trustService.removeProtector(request.userAnswers.utr, RemoveProtector(ProtectorType.BusinessProtector, index)).map(_ =>
                    Redirect(controllers.routes.AddAProtectorController.onPageLoad())
                  )
                } else {
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveYesNoPage, value))
                    _ <- repository.set(updatedAnswers)
                  } yield {
                    Redirect(controllers.business.remove.routes.WhenRemovedController.onPageLoad(index).url)
                  }
                }
            }
          } else {
            Future.successful(Redirect(controllers.routes.AddAProtectorController.onPageLoad().url))
          }
        }
      )
  }
}
