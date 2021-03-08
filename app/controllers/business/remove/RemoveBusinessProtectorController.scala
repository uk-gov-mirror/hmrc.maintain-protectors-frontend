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

package controllers.business.remove

import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.{ProtectorType, RemoveProtector}
import pages.business.RemoveYesNoPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.business.remove.RemoveBusinessProtectorView

import scala.concurrent.{ExecutionContext, Future}

class RemoveBusinessProtectorController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   repository: PlaybackRepository,
                                                   standardActionSets: StandardActionSets,
                                                   trustService: TrustService,
                                                   formProvider: YesNoFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: RemoveBusinessProtectorView,
                                                   errorHandler: ErrorHandler
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "removeBusinessProtector"

  private val form = formProvider.withPrefix(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      val preparedForm = request.userAnswers.get(RemoveYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      trustService.getBusinessProtector(request.userAnswers.identifier, index).map {
        protector =>
          Ok(view(preparedForm, index, protector.name))
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting business protector $index from trusts service ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(controllers.routes.AddAProtectorController.onPageLoad()))
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error getting business protector $index from trusts service ${e.getMessage}")

          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getBusinessProtector(request.userAnswers.identifier, index).map {
            protector =>
              BadRequest(view(formWithErrors, index, protector.name))
          }
        },
        value => {

          if (value) {

            trustService.getBusinessProtector(request.userAnswers.identifier, index).flatMap {
              protector =>
                if (protector.provisional) {
                  trustService.removeProtector(request.userAnswers.identifier, RemoveProtector(ProtectorType.BusinessProtector, index)).map(_ =>
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
