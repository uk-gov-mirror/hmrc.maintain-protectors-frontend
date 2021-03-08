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

package controllers

import config.FrontendAppConfig
import connectors.TrustsStoreConnector
import controllers.actions.StandardActionSets
import forms.{AddAProtectorFormProvider, YesNoFormProvider}
import javax.inject.Inject
import models.AddAProtector
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddAProtectorViewHelper
import views.html.{AddAProtectorView, AddAProtectorYesNoView, MaxedOutProtectorsView}
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class AddAProtectorController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         standardActionSets: StandardActionSets,
                                         val controllerComponents: MessagesControllerComponents,
                                         val appConfig: FrontendAppConfig,
                                         trustStoreConnector: TrustsStoreConnector,
                                         trustService: TrustService,
                                         addAnotherFormProvider: AddAProtectorFormProvider,
                                         yesNoFormProvider: YesNoFormProvider,
                                         repository: PlaybackRepository,
                                         addAnotherView: AddAProtectorView,
                                         yesNoView: AddAProtectorYesNoView,
                                         completeView: MaxedOutProtectorsView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val addAnotherForm : Form[AddAProtector] = addAnotherFormProvider()

  val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAProtectorYesNo")

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      for {
        protectors <- trustService.getProtectors(request.userAnswers.identifier)
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(updatedAnswers)
      } yield {
        val protectorRows = new AddAProtectorViewHelper(protectors).rows

        protectors.size match {
          case 0 =>
            Ok(yesNoView(yesNoForm))
          case _ if protectors.isNotMaxedOut =>
            Ok(addAnotherView(
              form = addAnotherForm,
              inProgressProtectors = protectorRows.inProgress,
              completeProtectors = protectorRows.complete,
              heading = protectors.addToHeading
            ))
          case _ if protectors.isMaxedOut =>
            Ok(completeView(
              inProgressProtectors = protectorRows.inProgress,
              completeProtectors = protectorRows.complete,
              heading = protectors.addToHeading
            ))
        }
      }
  }

  def submitOne(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors)))
        },
        addNow => {
          if (addNow) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
              _ <- repository.set(updatedAnswers)
            } yield Redirect(controllers.routes.AddNowController.onPageLoad())
          } else {
            for {
              _ <- trustStoreConnector.setTaskComplete(request.userAnswers.identifier)
            } yield {
              Redirect(appConfig.maintainATrustOverview)
            }
          }
        }
      )
  }

  def submitAnother(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      trustService.getProtectors(request.userAnswers.identifier).flatMap { protectors =>
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
                _ <- trustStoreConnector.setTaskComplete(request.userAnswers.identifier)
              } yield {
                Redirect(appConfig.maintainATrustOverview)
              }
          }
        )
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      for {
        _ <- trustStoreConnector.setTaskComplete(request.userAnswers.identifier)
      } yield {
        logger.info(s"[Session ID: ${Session.id(hc)}]" +
          s" user has finished maintaining protectors and is returning to the task list")
        Redirect(appConfig.maintainATrustOverview)
      }
  }
}
