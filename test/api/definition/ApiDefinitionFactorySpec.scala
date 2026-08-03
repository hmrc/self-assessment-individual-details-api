/*
 * Copyright 2023 HM Revenue & Customs
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

package api.definition

import api.config.Deprecation.NotDeprecated
import api.config.{AppConfig, MockAppConfig}
import api.definition.APIAccessType.{CONTROLLED, PUBLIC}
import api.definition.APIStatus.{ALPHA, BETA}
import api.mocks.MockHttpClient
import api.routing.*
import api.utils.UnitSpec
import cats.implicits.catsSyntaxValidatedId

import scala.language.reflectiveCalls

class ApiDefinitionFactorySpec extends UnitSpec {

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {

      s"return the expected status" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "BETA"

        val result: APIStatus = checkBuildApiStatus(Version9)
        result shouldBe BETA
      }

    }

    "the 'apiStatus' parameter is present but invalid" should {
      s"default to alpha" in new Test {
        setupMockConfig(Version9)
        MockedAppConfig.apiStatus(Version9) returns "not-a-status"

        checkBuildApiStatus(Version9) shouldBe ALPHA
      }
    }

    "the 'deprecatedOn' parameter is missing for a deprecated version" should {
      "throw an exception" in new Test {
        MockedAppConfig.apiStatus(Version9) returns "DEPRECATED"

        MockedAppConfig
          .deprecationFor(Version9)
          .returns("deprecatedOn date is required for a deprecated version".invalid)
          .anyNumberOfTimes()

        val exception: Exception = intercept[Exception] {
          checkBuildApiStatus(Version9)
        }

        val exceptionMessage: String = exception.getMessage
        exceptionMessage shouldBe "deprecatedOn date is required for a deprecated version"
      }
    }

    "set the access level" when {
      "the controlled access flag is enabled" should {
        "to be CONTROLLED" in new Test(controlledAccessEnabled = true) {
          MockedAppConfig.endpointsEnabled(Version3)
          MockedAppConfig.apiStatus(Version3) returns "BETA"
          MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()

          MockedAppConfig.controlledAccessEnabled returns true

          apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.CONTROLLED
        }
      }

      "the controlled access flag is disabled" should {
        "return PUBLIC" in new Test(controlledAccessEnabled = false) {
          MockedAppConfig.endpointsEnabled(Version3)
          MockedAppConfig.apiStatus(Version3) returns "BETA"
          MockedAppConfig.deprecationFor(Version3).returns(NotDeprecated.valid).anyNumberOfTimes()

          MockedAppConfig.controlledAccessEnabled returns false

          apiDefinitionFactory.definition.api.versions.head.access shouldBe APIAccessType.PUBLIC
        }
      }
    }
  }

  trait Test(controlledAccessEnabled: Boolean = true) extends UnitSpec with MockHttpClient with MockAppConfig {
    MockedAppConfig.apiGatewayContext returns "individuals/self-assessment/adjustable-summary"

    val access = if (controlledAccessEnabled) CONTROLLED else PUBLIC

    val apiDefinitionFactory: ApiDefinitionFactory = new ApiDefinitionFactory {
      protected val appConfig: AppConfig = mockAppConfig

      lazy val definition: Definition = Definition(
        APIDefinition(
          "test API definition",
          "description",
          "context",
          List("category"),
          List(APIVersion(Version1, APIStatus.BETA, access, endpointsEnabled = true)),
          None
        )
      )

    }

    def checkBuildApiStatus(version: Version): APIStatus = apiDefinitionFactory.buildAPIStatus(version)

    protected def setupMockConfig(version: Version): Unit = {
      MockedAppConfig
        .deprecationFor(version)
        .returns(NotDeprecated.valid)
        .anyNumberOfTimes()
    }

  }

}
