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

    When creating_preferences_with_currency_$_and_country_$(String currency, String country) {
        def httpClient = aut.instance.httpClient.requestSpec { request ->
            request.headers {
                it.set(CONTENT_TYPE, APPLICATION_JSON)
            }.body.text(toJson([customerId: customerId, currency: currency, country: country]))
        }

        assert httpClient.post("/api/preferences.create").status.code == 202
        self()
    }
}
