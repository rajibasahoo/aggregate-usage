package nl.tele2.fez;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "src/test/resources/features/component"
        },
        plugin = {"pretty", "json:target/cucumber.json", "html:target/cucumber", "junit:target/cucumber/report.xml"},
        glue = "nl.tele2.fez.component"
)
public class ComponentTests {

}