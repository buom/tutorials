package parts.code.streams.suppliers

import java.time.Clock
import java.util.UUID
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.slf4j.LoggerFactory
import parts.code.streams.schemas.CreatePreferencesCommand
import parts.code.streams.schemas.CreatePreferencesDenied
import parts.code.streams.schemas.PreferencesCreated

class PreferencesTransformer constructor(private val clock: Clock) :
    Transformer<String, SpecificRecord, KeyValue<String, SpecificRecord>?> {

    private val logger = LoggerFactory.getLogger(PreferencesTransformer::class.java)

    override fun init(context: ProcessorContext) {
    }

    override fun transform(key: String, record: SpecificRecord): KeyValue<String, SpecificRecord>? {
        if (record is CreatePreferencesCommand) {
            val eventId = UUID.randomUUID().toString()
            val event = if (record.currency == "USD") {
                PreferencesCreated(eventId, clock.instant(), record.customerId, record.currency, record.country)
            } else {
                CreatePreferencesDenied(eventId, clock.instant(), record.customerId, record.currency, record.country)
            }

            logger.info(
                "Transformed CreatePreferencesCommand to ${event.schema.name}" +
                        "\n\trecord to transform: $record" +
                        "\n\ttransformed to: $event"
            )

            return KeyValue(record.customerId, event)
        }

        return null
    }

    override fun close() {}
}
