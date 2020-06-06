package parts.code.interactive.queries.stages

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import parts.code.interactive.queries.ApplicationsUnderTest

import static groovy.json.JsonOutput.toJson
import static ratpack.http.MediaType.APPLICATION_JSON
import static ratpack.http.internal.HttpHeaderConstants.CONTENT_TYPE

class When extends Stage<When> {

    @ExpectedScenarioState String customerId
    @ExpectedScenarioState ApplicationsUnderTest aut
    @ExpectedScenarioState BigDecimal amount

    When adding_funds() {
        def httpClient = aut.instance1.httpClient.requestSpec { request ->
            request.headers {
                it.set(CONTENT_TYPE, APPLICATION_JSON)
            }.body.text(toJson([customerId: customerId, amount: amount]))
        }

        assert httpClient.post("/api/balance.addFunds").status.code == 202
        self()
    }
}
