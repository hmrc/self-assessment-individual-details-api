package api.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.Json
import support.WireMockMethods

object MtdIdLookupStub extends WireMockMethods {

  private def lookupUrl(nino: String): String = s"/mtd-identifier-lookup/nino/$nino"

  def ninoFound(nino: String): StubMapping =
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status = OK, body = Json.obj("mtdbsa" -> "12345678"))

  def error(nino: String, status: Int): StubMapping =
    when(method = GET, uri = lookupUrl(nino))
      .thenReturn(status, body = Json.obj())

}
