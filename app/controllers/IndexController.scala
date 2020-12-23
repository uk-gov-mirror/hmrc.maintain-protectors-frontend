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

import connectors.TrustConnector
import controllers.actions.StandardActionSets
import javax.inject.Inject
import models.UserAnswers
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: StandardActionSets,
                                 cacheRepository : PlaybackRepository,
                                 connector: TrustConnector)
                               (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(utr: String): Action[AnyContent] = (actions.auth andThen actions.saveSession(utr) andThen actions.getData).async {
      implicit request =>
        logger.info(s"[Session ID: ${Session.id(hc)}][UTR: $utr]" +
          s" user has started to maintain protectors")
        for {
          details <- connector.getTrustDetails(utr)
          ua <- Future.successful(
            request.userAnswers.getOrElse {
              UserAnswers(
                internalId = request.user.internalId,
                utr = utr,
                whenTrustSetup = details.startDate
              )
            }
          )
          _ <- cacheRepository.set(ua)
        } yield {
          Redirect(controllers.routes.AddAProtectorController.onPageLoad())
        }
    }
}
