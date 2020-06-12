package parts.code.interactive.queries

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

@Singleton
class KafkaTestUtils {

    private Map<String, KafkaConsumer<String, SpecificRecord>> consumers = [:]

    KafkaConsumer<String, SpecificRecord> consumer(String topic) {
        if (!consumers.containsKey(topic)) {
            def properties = new Properties()

            properties.with {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                put(CommonClientConfigs.GROUP_ID_CONFIG, "${UUID.randomUUID()}".toString())
                put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081")
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class)
                put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true")
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            }

            def consumer = new KafkaConsumer(properties)
            consumer.subscribe([topic])
            consumers.put(topic, consumer)
        }

        return consumers.get(topic)
    }
}
