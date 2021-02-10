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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.protectors.{BusinessProtector, IndividualProtector, Protectors}
import models.{Name, ProtectorType, RemoveProtector, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

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

  private val trustsUrl: String = "/trusts"
  private val protectorsUrl: String = s"$trustsUrl/protectors"

  private def getTrustDetailsUrl(utr: String) = s"$trustsUrl/$utr/trust-details"
  private def getProtectorsUrl(utr: String) = s"$protectorsUrl/$utr/transformed"
  private def addBusinessProtectorUrl(utr: String) = s"$protectorsUrl/add-business/$utr"
  private def amendBusinessProtectorUrl(utr: String, index: Int) = s"$protectorsUrl/amend-business/$utr/$index"
  private def addIndividualProtectorUrl(utr: String) = s"$protectorsUrl/add-individual/$utr"
  private def amendIndividualProtectorUrl(utr: String, index: Int) = s"/trusts/protectors/amend-individual/$utr/$index"
  private def removeProtectorUrl(utr: String) = s"$protectorsUrl/$utr/remove"

  private val individual = IndividualProtector(
    name = Name("Carmel", None, "Protector"),
    dateOfBirth = None,
    identification = None,
    address = None,
    entityStart = date,
    provisional = false
  )

  private val business = BusinessProtector(
    name = "Protector Org 24",
    utr = None,
    address = None,
    entityStart = date,
    provisional = false
  )

  "trust connector" when {

    "getTrustsDetails" in {

      val json = Json.parse(
        """
          |{
          | "startDate": "2019-02-03",
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
        get(urlEqualTo(getTrustDetailsUrl(utr)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(utr)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = date, typeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust)
      }

    }

    "getProtectors" when {

      "there are no protectors" must {

        "return a default empty list of protectors" in {

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
            get(urlEqualTo(getProtectorsUrl(utr)))
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

      "there are protectors" must {

        "parse the response and return the protectors" in {

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
              |       "entityStart" : "2019-02-03"
              |     }
              |   ],
              |   "protectorCompany" : [
              |     {
              |       "lineNo" : "110",
              |       "bpMatchStatus" : "98",
              |       "name" : "Protector Org 24",
              |       "companyType" : "Investment",
              |       "companyTime" : false,
              |       "entityStart" : "2019-02-03"
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
            get(urlEqualTo(getProtectorsUrl(utr)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getProtectors(utr)

          whenReady(processed) {
            result =>
              result mustBe
                Protectors(protector = List(individual),
                  protectorCompany = List(business)
                )
          }

          application.stop()
        }
      }

    }

    "addBusinessProtector" must {

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

        val result = connector.addBusinessProtector(utr, business)

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

        val result = connector.addBusinessProtector(utr, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "amendBusinessProtector" must {

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

        val result = connector.amendBusinessProtector(utr, index, business)

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

        val result = connector.amendBusinessProtector(utr, index, business)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "addIndividualProtector" must {

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

        val result = connector.addIndividualProtector(utr, individual)

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

        val result = connector.addIndividualProtector(utr, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }
    
    "amendIndividualProtector" must {

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

        val result = connector.amendIndividualProtector(utr, index, individual)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }

    "removeSettlor" must {

      def removeSettlor(protectorType: ProtectorType): RemoveProtector = RemoveProtector(protectorType, index, date)

      "Return OK when the request is successful" in {

        forAll(arbitraryProtectorType) {
          protectorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeProtectorUrl(utr)))
                .willReturn(ok)
            )

            val result = connector.removeProtector(utr, removeSettlor(protectorType))

            result.futureValue.status mustBe OK

            application.stop()
        }
      }

      "return Bad Request when the request is unsuccessful" in {

        forAll(arbitraryProtectorType) {
          settlorType =>

            val application = applicationBuilder()
              .configure(
                Seq(
                  "microservice.services.trusts.port" -> server.port(),
                  "auditing.enabled" -> false
                ): _*
              ).build()

            val connector = application.injector.instanceOf[TrustConnector]

            server.stubFor(
              put(urlEqualTo(removeProtectorUrl(utr)))
                .willReturn(badRequest)
            )

            val result = connector.removeProtector(utr, removeSettlor(settlorType))

            result.map(response => response.status mustBe BAD_REQUEST)

            application.stop()
        }
      }

    }

  }
}
