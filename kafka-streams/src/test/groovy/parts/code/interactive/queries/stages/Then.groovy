package parts.code.interactive.queries.stages

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import parts.code.interactive.queries.KafkaTestUtils
import parts.code.interactive.queries.Topics
import parts.code.streams.schemas.CreatePreferencesDenied
import parts.code.streams.schemas.PreferencesCreated
import spock.util.concurrent.PollingConditions

import java.time.Duration

class Then extends Stage<Then> {

    @ExpectedScenarioState String customerId

    Then create_preferences_with_currency_$_and_country_$(String currency, String country) {
        def consumer = KafkaTestUtils.instance.consumer(Topics.preferences)

        new PollingConditions(timeout: 30).eventually {
            def events = consumer.poll(Duration.ZERO).findResults { it.key() == customerId ? it.value() : null }
            assert !events.isEmpty()

            def event = events.last()
            assert event instanceof PreferencesCreated
            assert UUID.fromString(event.id)
            assert event.occurredOn != null
            assert event.customerId == customerId
            assert event.currency == currency
            assert event.country == country
        }

        self()
    }

    Then create_preferences_with_currency_$_and_country_$_is_denied(String currency, String country) {
        def consumer = KafkaTestUtils.instance.consumer(Topics.preferencesAuthorization)

        new PollingConditions(timeout: 10).eventually {
            def events = consumer.poll(Duration.ZERO).findResults { it.key() == customerId ? it.value() : null }
            assert !events.isEmpty()

            def event = events.last()
            assert event instanceof CreatePreferencesDenied
            assert UUID.fromString(event.id)
            assert event.occurredOn != null
            assert event.customerId == customerId
            assert event.currency == currency
            assert event.country == country
        }

        self()
    }
}
