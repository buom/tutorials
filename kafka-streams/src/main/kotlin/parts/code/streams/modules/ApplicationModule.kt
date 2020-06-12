package parts.code.streams.modules

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import java.time.Clock
import java.util.Properties
import javax.inject.Singleton
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.TransformerSupplier
import org.slf4j.LoggerFactory
import parts.code.streams.config.KafkaConfig
import parts.code.streams.handlers.CreatePreferencesHandler
import parts.code.streams.schemas.CreatePreferencesDenied
import parts.code.streams.schemas.PreferencesCreated
import parts.code.streams.services.KafkaStreamService
import parts.code.streams.suppliers.PreferencesTransformer

class ApplicationModule : AbstractModule() {

    private val logger = LoggerFactory.getLogger(ApplicationModule::class.java)

    override fun configure() {
        bind(KafkaStreamService::class.java)
        bind(CreatePreferencesHandler::class.java)
    }

    @Provides
    @Singleton
    fun provideKafkaStreams(config: KafkaConfig, clock: Clock): KafkaStreams {
        val builder = StreamsBuilder()

        val (preferences, preferencesAuthorization) = builder
            .stream<String, SpecificRecord>(config.topics.preferencesAuthorization)
            .transform(TransformerSupplier { PreferencesTransformer(clock) })
            .branch(
                Predicate { _, v -> v is PreferencesCreated },
                Predicate { _, v -> v is CreatePreferencesDenied }
            )

        preferences
            .peek { _, record -> logger.info("Sent ${record.schema.name} to topic: ${config.topics.preferences}\n\trecord: $record") }
            .to(config.topics.preferences)

        preferencesAuthorization
            .peek { _, record -> logger.info("Sent ${record.schema.name} to topic: ${config.topics.preferencesAuthorization}\n\trecord: $record") }
            .to(config.topics.preferencesAuthorization)

        val topology = builder.build()

        val properties = Properties()

        properties[StreamsConfig.APPLICATION_ID_CONFIG] = config.applicationId
        properties[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java
        properties[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        properties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServersConfig
        properties[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = config.schemaRegistryUrlConfig
        properties["value.subject.name.strategy"] = TopicRecordNameStrategy::class.java

        return KafkaStreams(topology, properties)
    }

    @Provides
    @Singleton
    fun provideKafkaProducer(kafkaConfig: KafkaConfig): KafkaProducer<String, SpecificRecord> {
        val properties = Properties().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServersConfig)
            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfig.schemaRegistryUrlConfig)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java)
            put("value.subject.name.strategy", TopicRecordNameStrategy::class.java)
        }

        return KafkaProducer(properties)
    }
}
