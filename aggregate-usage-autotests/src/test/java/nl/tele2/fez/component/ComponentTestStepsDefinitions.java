package nl.tele2.fez.component;

import com.github.tomakehurst.wiremock.matching.ContainsPattern;
import com.github.tomakehurst.wiremock.matching.MatchesJsonPathPattern;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import nl.tele2.fez.Config;
import nl.tele2.fez.stubs.Stub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@ContextConfiguration(classes = Config.class)
public class ComponentTestStepsDefinitions extends BaseDefinitions {

    @Autowired
    private Stub stub;

    @After
    public void afterScenario() {
        stub.reset();
        doCall("/hystrix/reset", HttpMethod.POST, null);
    }

    @When("^the client calls /health$")
    public void callHealthEndpoint() {
        doCall("/health", HttpMethod.GET, null);
    }

    @Then("^the service responds with \"(.*)\"$")
    public void theServiceRespondsWithHealthStatus(String status) {
        DocumentContext json = JsonPath.parse(savedResponseBody);
        assertThat(json.read("$.status"), containsString(status));
        assertThat(json.read("$.details.hystrix"), notNullValue());
    }

    @Given("^there is a customer with unlimited data and unlimited voice$")
    public void setupCustomerWithUnlimitedDataAndUnlimitedVoice() {
        stub.setupStub("stubs/unlimitedDataUnlimitedVoice.json", "stubs/restOfWorldWithTopups.json", "stubs/msisdns.json");
    }

    @Given("^there is a customer with data and voice$")
    public void setupCustomerWithDataAndVoice() {
        stub.setupStub("stubs/dataAndVoice.json", "stubs/restOfWorldWithTopups.json", "stubs/msisdns.json");
    }

    @Given("^there is a customer with data and voice and 24UL$")
    public void thereIsACustomerWithDataAndVoiceAndUL() {
        stub.setupStub("stubs/dataAnd24ULAndVoice.json", "stubs/restOfWorldWithTopups.json", "stubs/msisdns.json");
    }

    @When("^the client calls the aggregate usage service$")
    public void callAggregateUsageService() {
        doCall("/customers/1/msisdns/0600000000", HttpMethod.GET, null);
    }

    @When("^the client calls the aggregate usage service national endpoint$")
    public void callAggregateUsageServiceNational() {
        doCall("/customers/1/msisdns/0600000000/national", HttpMethod.GET, null);
    }


    @When("^the client send a text message to the aggregate usage service$")
    public void theClientSendATextMessageToTheAggregateUsageService() {
        doCall("/messages/balance", HttpMethod.POST, "{\"msisdn\": \"0600000000\"}");
    }

    @Then("^the service send a text message containing \"([^\"]*)\"$")
    public void theServiceSendATextMessageWithWithDataUsageRemaining(String textContent) throws Throwable {
        stub.verifyCalledWith(
                "/messaging/sms",
                new MatchesJsonPathPattern("$.message.context.CONTENT",
                        new ContainsPattern(textContent))
        );
    }

    @Then("^the service responds with the correct values for unlimited data and unlimited voice$")
    public void assertCorrectResponseUnlimitedDataAndUnlimitedVoiceNational() {
        DocumentContext json = JsonPath.parse(savedResponseBody);

        assertEquals(localizeAndFormat("2018-09-01 00:00:00.000+0200"), json.read("$.billingPeriod.startDate"));
        assertEquals(localizeAndFormat("2018-09-30 23:59:59.000+0200"), json.read("$.billingPeriod.endDate"));
        assertTrue(json.read("$.unlimitedVoice"));
        assertTrue(json.read("$.unlimitedData"));

        assertEquals("0.00", json.read("$.unbilledUsage.amount"));

        HashMap unlimitedData = getAsMap(json, "$.data");
        List<Map> bundles = (List<Map>) unlimitedData.get("bundles");
        assertDetailedBundleDetails(bundles.get(0), "DATA", "Unlimited Data", "MEGABYTES", localizeAndFormat("2018-09-10 00:00:00.000+0200"), localizeAndFormat("2018-09-10 00:00:00.000+0200"), localizeAndFormat("2018-09-10 23:59:59.000+0200"), "DAILY", 1234, 5000, 75, 3766,
                "ulnl");

        HashMap dataLimitEu = getAsMap(json, "$.dataLimitEu");
        assertDetailedBundleDetails(dataLimitEu, "DATA", "Datalimiet EU", "MEGABYTES", localizeAndFormat("2018-09-01 00:00:00.000+0200"), localizeAndFormat("2018-09-06 00:00:00.000+0200"), localizeAndFormat("2018-09-30 23:59:59.000+0200"), "BILL_CYCLE", 9000, 9000, 0, 0,
                "roaming_eu");

        HashMap extraCosts = getAsMap(json, "$.extraCosts");
        assertBaseBundleDetails(extraCosts, "EXTRA_COSTS", "Factuur Limiet", "MONEY",
                localizeAndFormat("2018-09-01 00:00:00.000+0200"), localizeAndFormat("2018-09-01 00:00:00.000+0200"), localizeAndFormat("2018-09-30 23:59:59.000+0200"));
        assertEquals("100.00", extraCosts.get("limit"));
        assertEquals("30.50", extraCosts.get("used"));

        Map unlimitedVoice = getAsMap(json, "$.voiceSms");
        assertDetailedBundleDetailsUnlimited("VOICE_SMS", "Unlimited Bel/SMS", "MINUTES_SMS", localizeAndFormat("2018-09-07 19:30:01.000+0200"), localizeAndFormat("2018-09-06 00:00:00.000+0200"), localizeAndFormat("2018-09-30 23:59:59.000+0200"), "BILL_CYCLE", unlimitedVoice);
    }

    @And("^the service responds with the correct rest of world bundles$")
    public void assertRestOfWorldBundles() {
        DocumentContext json = JsonPath.parse(savedResponseBody);

        List<Map> roamingAsList = getRoamingAsList(json);

        Map aggregatedRoze = roamingAsList.stream().filter(map -> (map.get("name")).equals("Roze Bundel")).findFirst().get();
        List<Map> bundles = (List<Map>) aggregatedRoze.get("bundles");

        assertDetailedBundleDetails(bundles.get(0), "DATA", "Roze Bundel (Klein)", "MEGABYTES", null, localizeAndFormatISODateTime("2018-09-11T10:08:27.000+02:00"), localizeAndFormatISODateTime("2018-10-12T10:08:26.000+02:00"), "ONE_TIME", 40, 40, 0, 0,
                "roze");
    }

    @Then("^the service responds with the correct values for data and voice$")
    public void assertCorrectResponseDataAndVoice() {
        DocumentContext json = JsonPath.parse(savedResponseBody);

        assertEquals(localizeAndFormat("2018-08-01 00:00:00.000+0200"), json.read("$.billingPeriod.startDate"));
        assertEquals(localizeAndFormat("2018-08-31 23:59:59.000+0200"), json.read("$.billingPeriod.endDate"));
        assertFalse(json.read("$.unlimitedVoice"));
        assertFalse(json.read("$.unlimitedVoice"));

        assertEquals("0.00", json.read("$.unbilledUsage.amount"));

        HashMap data = getAsMap(json, "$.data");

        assertEquals(999, data.get("remaining"));
        assertEquals(10000, data.get("limit"));
        assertEquals(90, data.get("usedPercent"));
        assertEquals(9001, data.get("used"));


        List<Map> bundles = (List<Map>) data.get("bundles");

        Map dataBundle = bundles.stream().filter(bundl -> bundl.get("type").equals("DATA")).findFirst().get();
        assertDetailedBundleDetails(dataBundle, "DATA", "Data", "MEGABYTES", localizeAndFormat("2018-08-01 00:00:00.000+0200"),
                localizeAndFormat("2018-08-01 00:00:00.000+0200"), localizeAndFormat("2018-08-31 23:59:59.000+0200"), "BILL_CYCLE", 999,10000, 90, 9001, "nleu" );

        assertBundleDeailsForDataSharing(dataBundle);
        HashMap extraCosts = getAsMap(json, "$.extraCosts");
        assertBaseBundleDetails(extraCosts, "EXTRA_COSTS", "Factuur Limiet", "MONEY",
                localizeAndFormat("2018-08-01 00:00:00.000+0200"), localizeAndFormat("2018-08-01 00:00:00.000+0200"), localizeAndFormat("2018-08-31 23:59:59.000+0200"));
        assertEquals("100.00", extraCosts.get("limit"));
        assertEquals("30.50", extraCosts.get("used"));
    }

    private void assertBundleDeailsForDataSharing(Map unlimitedData) {
        assertEquals("958268", unlimitedData.get("entitlementId"));
        assertEquals(100, unlimitedData.get("recurrenceAmount"));
        assertEquals("DMRB_R120190103144136", unlimitedData.get("crmRef"));
    }

    private HashMap getAsMap(DocumentContext json, String field) {
        return json.read(field);
    }

    private List<Map> getRoamingAsList(DocumentContext json) {
        return json.read("$.roamingBundles");
    }

    private void assertDetailedBundleDetailsUnlimited(String type, String bundleName, String unit, String lastUsage, String startDate, String endDate, String repeatType, Map bundle) {
        assertDetailedBundleDetails(bundle, type, bundleName, unit, lastUsage, startDate, endDate, repeatType, null, null, null, null, null);
    }

    private void assertDetailedBundleDetails(Map bundle, String type, String bundleName, String unit, String lastUsage, String startDate, String endDate, String repeatType, Integer remaining, Integer limit, Integer usedPercent, Integer used, String zone) {
        assertBaseBundleDetails(bundle, type, bundleName, unit, lastUsage, startDate, endDate);
        assertEquals(repeatType, bundle.get("repeatType"));
        assertEquals(remaining, bundle.get("remaining"));
        assertEquals(limit, bundle.get("limit"));
        assertEquals(usedPercent, bundle.get("usedPercent"));
        assertEquals(used, bundle.get("used"));
        assertEquals(zone, bundle.get("zone"));
    }

    private void assertBaseBundleDetails(Map bundle, String type, String bundleName, String unit, String lastUsage, String startDate, String endDate) {
        assertEquals(type, bundle.get("type"));
        assertEquals(bundleName, bundle.get("name"));
        assertEquals(unit, bundle.get("unit"));
        assertEquals(lastUsage, bundle.get("lastUsage"));
        assertEquals(startDate, bundle.get("startDate"));
        assertEquals(endDate, bundle.get("endDate"));
    }

    private String localizeAndFormat(String dateTime) {
        return ZonedDateTime.parse(dateTime, DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS[X]"))
                .withZoneSameInstant(ZoneId.of("Europe/Amsterdam"))
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private String localizeAndFormatISODateTime(String dateTime) {
        return ZonedDateTime.parse(dateTime)
                .withZoneSameInstant(ZoneId.of("Europe/Amsterdam"))
                .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    @And("^the response contains FUP$")
    public void theResponseContainsFUP() {

        DocumentContext json = JsonPath.parse(savedResponseBody);

        HashMap data = getAsMap(json, "$.dataLimitEu");
        assertEquals(1100, data.get("remaining"));
        assertEquals(1100, data.get("limit"));
        assertEquals(0, data.get("usedPercent"));
        assertEquals(0, data.get("used"));

    }

    @And("^the 24UL bundle is the first bundle in the list$")
    public void containsTheULBundle() {
        DocumentContext json = JsonPath.parse(savedResponseBody);

        HashMap root = getAsMap(json, "$");
        assertFalse((boolean) root.get("unlimitedData"));

        HashMap data = getAsMap(json, "$.data");

        assertTrue((boolean) data.get("unlimited24Hours"));

        List<Map> bundles = (List<Map>) data.get("bundles");

        Map bundle = bundles.get(0);

        assertEquals("DATA_UNLIMITED", bundle.get("type"));

    }
}
