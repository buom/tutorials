package parts.code.interactive.queries

import com.tngtech.jgiven.spock.ScenarioSpec
import org.junit.AfterClass
import org.junit.BeforeClass
import parts.code.interactive.queries.stages.Given
import parts.code.interactive.queries.stages.Then
import parts.code.interactive.queries.stages.When
import spock.lang.Shared
import spock.util.concurrent.PollingConditions

class IntegrationTests extends ScenarioSpec<Given, When, Then> {

    @Shared
    ApplicationsUnderTest applicationsUnderTest = new ApplicationsUnderTest()

    @BeforeClass
    void start() {
        new PollingConditions(timeout: 30).eventually {
            assert applicationsUnderTest.started()
        }
    }

    @AfterClass
    void tearDown() {
        applicationsUnderTest.close()
    }

    def "create preferences if currency is USD"() {
        expect:
        given().applicationsUnderTest(applicationsUnderTest)
        when().creating_preferences_with_currency_$_and_country_$("USD", "US")
        then().create_preferences_with_currency_$_and_country_$("USD", "US")
    }

    def "deny create preferences if currency is not USD"() {
        expect:
        given().applicationsUnderTest(applicationsUnderTest)
        when().creating_preferences_with_currency_$_and_country_$("EUR", "US")
        then().create_preferences_with_currency_$_and_country_$_is_denied("EUR", "US")
    }
}
