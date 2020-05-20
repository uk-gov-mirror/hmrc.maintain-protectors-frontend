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

import java.time.LocalDate

import base.SpecBase
import connectors.TrustStoreConnector
import forms.{AddAProtectorFormProvider, YesNoFormProvider}
import models.protectors.{BusinessProtector, IndividualProtector, Protectors}
import models.{AddAProtector, Name, NationalInsuranceNumber, RemoveProtector}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddAProtectorViewHelper
import viewmodels.addAnother.AddRow
import views.html.{AddAProtectorView, AddAProtectorYesNoView, MaxedOutProtectorsView}

import scala.concurrent.{ExecutionContext, Future}

class AddAProtectorControllerSpec extends SpecBase with ScalaFutures {

  lazy val getRoute : String = controllers.routes.AddAProtectorController.onPageLoad().url
  lazy val submitOneRoute : String = controllers.routes.AddAProtectorController.submitOne().url
  lazy val submitAnotherRoute : String = controllers.routes.AddAProtectorController.submitAnother().url
  lazy val submitCompleteRoute : String = controllers.routes.AddAProtectorController.submitComplete().url

  val mockStoreConnector : TrustStoreConnector = mock[TrustStoreConnector]

  val addProtectorForm = new AddAProtectorFormProvider()()
  val addProtectorYesNoForm = new YesNoFormProvider().withPrefix("addAProtectorYesNo")

  private def individualProtector(provisional: Boolean) = IndividualProtector(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = provisional
  )

  private def businessProtector(provisional: Boolean) = BusinessProtector(
    name = "Humanitarian Company Ltd",
    utr = None,
    address = None,
    entityStart = LocalDate.parse("2012-03-14"),
    provisional = provisional
  )

  private val protectors = Protectors(List(individualProtector(true)), List(businessProtector(true)))

  lazy val featureNotAvailable : String = controllers.routes.FeatureNotAvailableController.onPageLoad().url

  val protectorRows = List(
    AddRow("First Last", typeLabel = "Individual protector", "Change details", Some(controllers.individual.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", Some(controllers.individual.remove.routes.RemoveIndividualProtectorController.onPageLoad(0).url)),
    AddRow("Humanitarian Company Ltd", typeLabel = "Business protector", "Change details", Some(controllers.business.amend.routes.CheckDetailsController.extractAndRender(0).url), "Remove", Some(controllers.business.remove.routes.RemoveBusinessProtectorController.onPageLoad(0).url))
  )

  class FakeService(data: Protectors) extends TrustService {
    override def getProtectors(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Protectors] = Future.successful(data)

    override def getIndividualProtector(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[IndividualProtector] =
      Future.successful(individualProtector(false))

    override def getBusinessProtector(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessProtector] =
      Future.successful(businessProtector(false))

    override def removeProtector(utr: String, protector: RemoveProtector)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(OK))
  }

  "AddAProtector Controller" when {

    "no protectors" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(Protectors(Nil, Nil))

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAProtector.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(Protectors(Nil, Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAProtectorYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addProtectorYesNoForm
          )(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user answers no" in {

        val fakeService = new FakeService(Protectors(Nil, Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request =
          FakeRequest(POST, submitOneRoute)
            .withFormUrlEncodedBody(("value", "false"))

        when(mockStoreConnector.setTaskComplete(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the what type (add now) when the user answers yes" in {

        val fakeService = new FakeService(Protectors(Nil, Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
          )).build()

        val request =
          FakeRequest(POST, submitOneRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddNowController.onPageLoad().url

        application.stop()
      }

    }

    "there are protectors" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAProtectorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addProtectorForm,
            Nil,
            protectorRows,
            "The trust has 2 protectors"
          )(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAProtector.NoComplete.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the maintain task list when the user says they want to add later" ignore {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAProtector.YesLater.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the what type (add now) when the user answers yes now" in {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
          )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAProtector.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddNowController.onPageLoad().url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addProtectorForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAProtectorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            Nil,
            protectorRows,
            "The trust has 2 protectors"
          )(fakeRequest, messages).toString

        application.stop()
      }
    }

    "maxed out protectors" must {

      "return OK and the correct view for a GET" in {

        val protectors = Protectors(List.fill(12)(individualProtector(true)), List.fill(13)(businessProtector(true)))

        val fakeService = new FakeService(protectors)

        val protectorRows = new AddAProtectorViewHelper(protectors).rows

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutProtectorsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(
            protectorRows.inProgress,
            protectorRows.complete,
            protectors.addToHeading
          )(fakeRequest, messages).toString
        content must include("You cannot enter another protector as you have entered a maximum of 25.")
        content must include("If you have further protectors to add, write to HMRC with their details.")

        application.stop()

      }

      "redirect to add to page and set protectors to complete when user clicks continue" in {

        val fakeService = new FakeService(protectors)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request = FakeRequest(POST, submitCompleteRoute)

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()

      }
    }
  }
}
