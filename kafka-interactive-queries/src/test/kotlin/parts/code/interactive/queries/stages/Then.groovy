package parts.code.interactive.queries.stages

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import parts.code.interactive.queries.ApplicationsUnderTest
import parts.code.interactive.queries.KafkaTestUtils
import parts.code.interactive.queries.Topics
import parts.code.interactive.queries.schemas.FundsAdded
import ratpack.http.MediaType
import ratpack.http.internal.HttpHeaderConstants
import spock.util.concurrent.PollingConditions

import java.time.Duration

import static groovy.json.JsonOutput.toJson

class Then extends Stage<Then> {

    @ExpectedScenarioState String customerId
    @ExpectedScenarioState ApplicationsUnderTest aut

    Then $_worth_of_funds_are_added(BigDecimal amount) {
        def consumer = KafkaTestUtils.instance.consumer(Topics.balance)

        new PollingConditions(timeout: 30).eventually {
            def events = consumer.poll(Duration.ZERO).findResults { it.key() == customerId ? it.value() : null }
            assert !events.isEmpty()

            def event = events.last()
            assert event instanceof FundsAdded
            assert UUID.fromString(event.id)
            assert event.occurredOn != null
            assert event.customerId == customerId
            assert event.amount == amount
        }

        self()
    }

    Then the_customer_balance_is_$(BigDecimal amount) {
        def httpClient = aut.instance1.httpClient.requestSpec { request ->
            request.headers {
                it.set(HttpHeaderConstants.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            }
        }.params {
            it.put("customerId", customerId)
        }

        def response = httpClient.get("/api/customers.getBalance")
        assert response.status.code == 200
        assert response.body.text == toJson([amount: amount])
        self()
    }
}
