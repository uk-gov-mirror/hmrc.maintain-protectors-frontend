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

package controllers

import config.FrontendAppConfig
import connectors.TrustStoreConnector
import controllers.actions.StandardActionSets
import forms.AddAProtectorFormProvider
import javax.inject.Inject
import models.AddAProtector
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddAProtectorViewHelper
import views.html.{AddAProtectorView, MaxedOutProtectorsView}

import scala.concurrent.{ExecutionContext, Future}

class AddAProtectorController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       val controllerComponents: MessagesControllerComponents,
                                       val appConfig: FrontendAppConfig,
                                       trustStoreConnector: TrustStoreConnector,
                                       trustService: TrustService,
                                       addAnotherFormProvider: AddAProtectorFormProvider,
                                       repository: PlaybackRepository,
                                       addAnotherView: AddAProtectorView,
                                       completeView: MaxedOutProtectorsView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val addAnotherForm : Form[AddAProtector] = addAnotherFormProvider()

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForUtr.async {
    implicit request =>

      for {
        protectors <- trustService.getProtectors(request.userAnswers.utr)
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(updatedAnswers)
      } yield {
        val protectorRows = new AddAProtectorViewHelper(protectors).rows

        if (protectors.isMaxedOut) {
          Ok(completeView(
            inProgressProtectors = protectorRows.inProgress,
            completeProtectors = protectorRows.complete,
            size = protectors.size
          ))
        } else {
          Ok(addAnotherView(
            form = addAnotherForm,
            inProgressProtectors = protectorRows.inProgress,
            completeProtectors = protectorRows.complete,
            heading = protectors.addToHeading
          ))
        }
      }
  }

  def submit(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getProtectors(request.userAnswers.utr).flatMap { protectors =>
        addAnotherForm.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {

            val rows = new AddAProtectorViewHelper(protectors).rows

            Future.successful(BadRequest(
              addAnotherView(
                formWithErrors,
                rows.inProgress,
                rows.complete,
                protectors.addToHeading
              )
            ))
          },
          {
            case AddAProtector.YesNow =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
                _ <- repository.set(updatedAnswers)
              } yield Redirect(controllers.routes.AddNowController.onPageLoad())

            case AddAProtector.YesLater =>
              Future.successful(Redirect(appConfig.maintainATrustOverview))

            case AddAProtector.NoComplete =>
              for {
                _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
              } yield {
                Redirect(appConfig.maintainATrustOverview)
              }
          }
        )
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      for {
        _ <- trustStoreConnector.setTaskComplete(request.userAnswers.utr)
      } yield {
        Redirect(appConfig.maintainATrustOverview)
      }
  }
}
