package nl.tele2.fez.aggregateusage

import java.net.URL

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scalaj.http.Http

import scala.concurrent.duration._
import scala.io.Source

class AggregateUsageScenario extends Simulation {
  private val wiremockBaseUrl = "http://wiremock-tip-aggregate-usage.tst.dle.nl.corp.tele2.com"
  private val httpConf = http.baseURL("http://aggregate-usage.tst.dle.nl.corp.tele2.com")

  private val scenarioAll = scenario("Get Usage - All")
    .exec(http("get_usage")
      .get("/customers/1/msisdns/0600000000")
      .header("Content-Type", "application/json")
      .header("BusinessProcessId", session => "ELON-TEST-BPID")
      .header("ConversationId", session => "ELON-TEST-CID")
      .check(status.is(200)))
    .exec((session: io.gatling.core.session.Session) => {
      session
    })

  private val scenarioNational = scenario("Get Usage - National")
    .exec(http("get_usage_national")
      .get("/customers/1/msisdns/0600000000/national")
      .header("Content-Type", "application/json")
      .header("BusinessProcessId", session => "ELON-TEST-BPID")
      .header("ConversationId", session => "ELON-TEST-CID")
      .check(status.is(200)))
    .exec((session: io.gatling.core.session.Session) => {
      session
    })

  private def setupStub(wireMockFileName: String, bodyFilePath: String, mappingFilePath: String) = {
    val bodyFileUrl: URL = getClass.getClassLoader.getResource(bodyFilePath)
    val bodyFileContent: String = Source.fromURL(bodyFileUrl).getLines.mkString

    val mappingFileUrl: URL = getClass.getClassLoader.getResource(mappingFilePath)
    val mappingFileContent: String = Source.fromURL(mappingFileUrl).getLines.mkString

    val bodyFileName = wiremockBaseUrl + "/__admin/files/" + wireMockFileName
    val deleteResult = Http(bodyFileName).method("delete").asParamMap.code
    println("Deleted " + bodyFileName + " -> " + deleteResult);

    val putResult = Http(bodyFileName).put(bodyFileContent).asParamMap.code
    println("Added " + bodyFileName + " -> " + putResult);

    val mappingResult = Http(wiremockBaseUrl + "/__admin/mappings").postData(mappingFileContent).asParamMap.code
    println("Added mapping " + mappingFilePath + " -> " + mappingResult);

    println("Stub setup done\n")
  }

  before(
    setupStub(
      "nationalDataAndVoice.xml",
      "__files/nationalDataAndVoice.xml",
      "mappings/dataAndVoice.json"
    )
  )
  before(
    setupStub(
      "msisdns.json",
      "__files/msisdns.json",
      "mappings/msisdns.json",
    )
  )
  before(
    setupStub(
      "restOfWorldWithTopups.xml",
      "__files/restOfWorldWithTopups.xml",
      "mappings/restOfWorldWithTopups.json"
    )
  )
  setUp(
    scenarioAll.inject(nothingFor(6 seconds), constantUsersPerSec(5) during (15 seconds)).protocols(httpConf),
    scenarioNational.inject(nothingFor(6 seconds), constantUsersPerSec(5) during (15 seconds)).protocols(httpConf)
  ).assertions(
    details("get_usage").responseTime.mean.lt(1000),
    global.successfulRequests.percent.is(100)
  )
}