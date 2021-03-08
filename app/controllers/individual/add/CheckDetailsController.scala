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

package controllers.individual.add

import config.FrontendAppConfig
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.individual.NameRequiredAction
import handlers.ErrorHandler
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.mappers.IndividualProtectorMapper
import utils.print.IndividualProtectorPrintHelper
import viewmodels.AnswerSection
import views.html.individual.add.CheckDetailsView

import scala.concurrent.{ExecutionContext, Future}

class CheckDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: CheckDetailsView,
                                        connector: TrustsConnector,
                                        val appConfig: FrontendAppConfig,
                                        printHelper: IndividualProtectorPrintHelper,
                                        mapper: IndividualProtectorMapper,
                                        nameAction: NameRequiredAction,
                                        errorHandler: ErrorHandler
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.andThen(nameAction) {
    implicit request =>

      val section: AnswerSection = printHelper(request.userAnswers, provisional = true, request.protectorName)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(protector) =>
          connector.addIndividualProtector(request.userAnswers.identifier, protector).map(_ =>
            Redirect(controllers.routes.AddAProtectorController.onPageLoad())
          )
      }
  }
}
