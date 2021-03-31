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

import java.time.LocalDate

import base.SpecBase
import connectors.TrustsConnector
import forms.YesNoFormProvider
import models.protectors.{BusinessProtector, Protectors}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.business.RemoveYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.business.remove.RemoveBusinessProtectorView

import scala.concurrent.Future

class RemoveBusinessProtectorControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  val messagesPrefix = "removeBusinessProtector"

  lazy val formProvider = new YesNoFormProvider()
  lazy val form = formProvider.withPrefix(messagesPrefix)

  lazy val name : String = "Some Name 1"

  val mockConnector: TrustsConnector = mock[TrustsConnector]

  def businessProtector(id: Int, provisional : Boolean) = BusinessProtector(
    name = s"Some Name $id",
    utr = None,
    countryOfResidence = None,
    address = None,
    entityStart = LocalDate.parse("2012-03-14"),
    provisional = provisional
  )

  val expectedResult = businessProtector(2, provisional = true)

  val protectors = List(
    businessProtector(1, provisional = false),
    expectedResult,
    businessProtector(3, provisional = true)
  )

  "RemoveBusinessProtector Controller" when {

    "return OK and the correct view for a GET" in {

      val index = 0

      when(mockConnector.getProtectors(any())(any(), any()))
        .thenReturn(Future.successful(Protectors(Nil, protectors)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessProtectorController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveBusinessProtectorView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, name)(request, messages).toString

      application.stop()
    }

    "redirect to the add protector page if we get an Index Not Found Exception" in {

      val userAnswers = emptyUserAnswers
        .set(RemoveYesNoPage, true).success.value

      when(mockConnector.getProtectors(any())(any(), any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("")))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[TrustsConnector].toInstance(mockConnector)).build()

      val request = FakeRequest(GET, routes.RemoveBusinessProtectorController.onPageLoad(0).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.AddAProtectorController.onPageLoad().url

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(RemoveYesNoPage, true).success.value

      when(mockConnector.getProtectors(any())(any(), any()))
        .thenReturn(Future.successful(Protectors(Nil, protectors)))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(bind[TrustsConnector].toInstance(mockConnector)).build()

      val request = FakeRequest(GET, routes.RemoveBusinessProtectorController.onPageLoad(0).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveBusinessProtectorView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), 0, name)(request, messages).toString

      application.stop()
    }

    "not removing the protector" must {

      "redirect to the add to page when valid data is submitted" in {

        val index = 0

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveBusinessProtectorController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddAProtectorController.onPageLoad().url

        application.stop()
      }
    }

    "removing an existing protector" must {

      "redirect to the next page when valid data is submitted" in {

        val index = 0

        when(mockConnector.getProtectors(any())(any(), any()))
          .thenReturn(Future.successful(Protectors(Nil, protectors)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveBusinessProtectorController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.business.remove.routes.WhenRemovedController.onPageLoad(0).url

        application.stop()
      }
    }

    "removing a new protector" must {

      "redirect to the add to page, removing the protector" in {

        val index = 2

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getProtectors(any())(any(), any()))
          .thenReturn(Future.successful(Protectors(Nil, protectors)))

        when(mockConnector.removeProtector(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val request =
          FakeRequest(POST, routes.RemoveBusinessProtectorController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.AddAProtectorController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustsConnector].toInstance(mockConnector)).build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessProtectorController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveBusinessProtectorView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, name)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveBusinessProtectorController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessProtectorController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
