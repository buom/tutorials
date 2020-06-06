package parts.code.interactive.queries.stages

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import parts.code.interactive.queries.ApplicationsUnderTest

class Given extends Stage<Given> {

    @ProvidedScenarioState String customerId = UUID.randomUUID().toString()
    @ProvidedScenarioState ApplicationsUnderTest aut
    @ProvidedScenarioState BigDecimal amount

    @Hidden
    Given applicationsUnderTest(ApplicationsUnderTest aut) {
        this.aut = aut
        self()
    }

    Given $_worth_of_funds(BigDecimal amount) {
        this.amount = amount
        self()
    }
}
