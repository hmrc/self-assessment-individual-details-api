/*
 * Copyright 2025 HM Revenue & Customs
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

package shared.definition

import play.api.libs.json.{JsValue, Json}
import shared.routing.Version3
import shared.utils.UnitSpec

class ApiModelSpec extends UnitSpec {

  private val access: Access               = Access("restricted", Seq("01", "04", "05"))
  private val parameter: Parameter         = Parameter("Some parameter", true)
  private val anotherParameter: Parameter  = Parameter("Another parameter")
  private val apiVersion: APIVersion       = APIVersion(Version3, APIStatus.ALPHA, endpointsEnabled = true)
  private val apiDefinition: APIDefinition = APIDefinition("b", "c", "d", List("category"), List(apiVersion), Some(false))
  private val definition                   = Definition(apiDefinition)

  private val accessJson: JsValue = Json.parse(s"""
     |{
     | "type": "restricted",
     | "whitelistedApplicationIds": ["01", "04", "05"]
     |}
     |""".stripMargin)

  private val parameterJson: JsValue = Json.parse(s"""
     |{
     | "name": "Some parameter",
     | "required": true
     |}
     |""".stripMargin)

  private val anotherParameterJson: JsValue = Json.parse(s"""
     |{
     | "name": "Another parameter",
     | "required": false
     |}
     |""".stripMargin)

  private val apiVersionJson: JsValue = Json.parse(s"""
       |{
       | "version": "3.0",
       |"status": "ALPHA",
       |"endpointsEnabled": true
       |}
       |""".stripMargin)

  private val apiDefinitionJson: JsValue = Json.parse(s"""{
       |"name": "b",
       |"description": "c",
       |"context": "d",
       |"categories": ["category"],
       |"versions": [$apiVersionJson],
       |"requiresTrust": false
       |}
       |""".stripMargin)

  private val definitionJson: JsValue = Json.parse(s"""{
       |"api": $apiDefinitionJson
       |}
       |""".stripMargin)

  "Access" when {
    "the full model is present" should {
      "correctly write the model to json" in {
        Json.toJson(access) shouldBe accessJson
      }
    }

    "the full Json is present" should {
      "correctly read JSON to a model" in {
        accessJson.as[Access] shouldBe access
      }
    }
  }

  "Parameter" when {
    "the full model is present" should {
      "correctly write the model to json" in {
        Json.toJson(parameter) shouldBe parameterJson
      }
    }

    "the minimal model is present" should {
      "correctly write the model to json" in {
        Json.toJson(anotherParameter) shouldBe anotherParameterJson
      }
    }

    "the full Json is present" should {
      "correctly read JSON to a model" in {
        parameterJson.as[Parameter] shouldBe parameter
      }
    }
  }

  "Definition" when {
    "the full model is present" should {
      "correctly write the model to json" in {
        Json.toJson(definition) shouldBe definitionJson
      }
    }

    "the full Json is present" should {
      "correctly read JSON to a model" in {
        definitionJson.as[Definition] shouldBe definition
      }
    }
  }

}
