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

    @Shared ApplicationsUnderTest applicationsUnderTest = new ApplicationsUnderTest()

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

    def "add funds to a customer"() {
        expect:
        given().applicationsUnderTest(applicationsUnderTest)
               .$_worth_of_funds(1.00)
        when().adding_funds()
        then().$_worth_of_funds_are_added(1.00)
              .and().the_customer_balance_is_$(1.00)
    }
}
