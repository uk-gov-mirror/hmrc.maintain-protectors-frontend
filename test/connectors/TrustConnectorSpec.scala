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

package connectors

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.protectors.{BusinessProtector, IndividualProtector, Protectors}
import models.{Name, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  val utr = "1000000008"
  val index = 0
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  "trust connector" when {

    "get trusts details" in {

      val utr = "1000000008"

      val json = Json.parse(
        """
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts/$utr/trust-details"))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(utr)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = LocalDate.parse("1920-03-28"), typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust)
      }

    }

    "get protectors returns a trust with empty lists" must {

      "return a default empty list protectors" in {

        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "protectors": {
            | }
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/protectors"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getProtectors(utr)

        whenReady(processed) {
          result =>
            result mustBe Protectors(protector = Nil, protectorCompany = Nil)
        }

        application.stop()
      }

    }

    "get protectors" must {

      "parse the response and return the protectors" in {
        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "protectors" : {
            |   "protector" : [
            |     {
            |       "lineNo" : "79",
            |       "name" : {
            |         "firstName" : "Carmel",
            |         "lastName" : "Protector"
            |       },
            |       "entityStart" : "2019-09-23"
            |     }
            |   ],
            |   "protectorCompany" : [
            |     {
            |       "lineNo" : "110",
            |       "bpMatchStatus" : "98",
            |       "name" : "Protector Org 24",
            |       "companyType" : "Investment",
            |       "companyTime" : false,
            |       "entityStart" : "2019-09-23"
            |     }
            |   ],
            |    "deceased" : {
            |       "name" : {
            |         "firstName" : "Carmel",
            |         "lastName" : "Protector"
            |       }
            |     }
            | }
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/protectors"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getProtectors(utr)

        whenReady(processed) {
          result =>
            result mustBe
              Protectors(protector = List(
                IndividualProtector(
                  name = Name("Carmel", None, "Protector"),
                  dateOfBirth = None,
                  identification = None,
                  address = None,
                  entityStart = LocalDate.parse("2019-09-23"),
                  provisional = false
                )
              ),
              protectorCompany = List(
                BusinessProtector(
                  name = "Protector Org 24",
                  utr = None,
                  address = None,
                  entityStart = LocalDate.parse("2019-09-23"),
                  provisional = false
                )
              )
            )
        }

        application.stop()
      }
    }

    "add business protector" must {

      def addBusinessProtectorUrl(utr: String) =
        s"/trusts/protectors/add-business/$utr"

      val protector = BusinessProtector(
        name = "Name",
        utr = None,
        address = None,
        entityStart = LocalDate.parse("2020-03-27"),
        provisional = false
      )

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessProtectorUrl(utr)))
            .willReturn(ok)
        )

        val result = connector.addBusinessProtector(utr, protector)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessProtectorUrl(utr)))
            .willReturn(badRequest)
        )

        val result = connector.addBusinessProtector(utr, protector)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amending a business protector" must {

      def amendBusinessProtectorUrl(utr: String, index: Int) =
        s"/trusts/protectors/amend-business/$utr/$index"

      val protector = BusinessProtector(
        name = "Name",
        utr = None,
        address = None,
        entityStart = LocalDate.parse("2020-03-27"),
        provisional = false
      )

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessProtectorUrl(utr, index)))
            .willReturn(ok)
        )

        val result = connector.amendBusinessProtector(utr, index, protector)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessProtectorUrl(utr, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendBusinessProtector(utr, index, protector)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "add individual protector" must {

      def addIndividualProtectorUrl(utr: String) =
        s"/trusts/protectors/add-individual/$utr"

      val protector = IndividualProtector(
        name = Name(
          firstName = "First",
          middleName = None,
          lastName = "Last"
        ),
        dateOfBirth = None,
        identification = None,
        address = None,
        entityStart = LocalDate.parse("2020-03-27"),
        provisional = false
      )

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addIndividualProtectorUrl(utr)))
            .willReturn(ok)
        )

        val result = connector.addIndividualProtector(utr, protector)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(addIndividualProtectorUrl(utr)))
            .willReturn(badRequest)
        )

        val result = connector.addIndividualProtector(utr, protector)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }
    
    "amending an individual protector" must {

      def amendIndividualProtectorUrl(utr: String, index: Int) =
        s"/trusts/protectors/amend-individual/$utr/$index"

      "Return OK when the request is successful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualProtectorUrl(utr, index)))
            .willReturn(ok)
        )

        val individual = IndividualProtector(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          identification = None,
          address = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendIndividualProtector(utr, index, individual)

        result.futureValue.status mustBe OK

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          post(urlEqualTo(amendIndividualProtectorUrl(utr, index)))
            .willReturn(badRequest)
        )

        val individual = IndividualProtector(
          name = Name(
            firstName = "First",
            middleName = None,
            lastName = "Last"
          ),
          dateOfBirth = None,
          identification = None,
          address = None,
          entityStart = LocalDate.parse("2020-03-27"),
          provisional = false
        )

        val result = connector.amendIndividualProtector(utr, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

  }
}
