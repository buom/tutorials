package parts.code.interactive.queries.modules

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.Stores
import parts.code.interactive.queries.config.KafkaConfig
import parts.code.interactive.queries.handlers.AddFundsHandler
import parts.code.interactive.queries.handlers.GetBalanceHandler
import parts.code.interactive.queries.schemas.BalanceState
import parts.code.interactive.queries.services.KafkaStreamService
import parts.code.interactive.queries.streams.suppliers.BalanceProcessor
import java.util.*
import javax.inject.Singleton

class ApplicationModule : AbstractModule() {

    override fun configure() {
        bind(KafkaStreamService::class.java)
        bind(BalanceProcessor::class.java)
        bind(AddFundsHandler::class.java)
        bind(GetBalanceHandler::class.java)
    }

    @Provides
    @Singleton
    fun provideKafkaStreams(
        config: KafkaConfig,
        balanceProcessor: BalanceProcessor
    ): KafkaStreams {
        val builder = StreamsBuilder()

        builder
            .addBalanceStateStore(config)
            .stream<String, SpecificRecord>(config.topics.balance)
            .process(ProcessorSupplier { balanceProcessor }, config.stateStores.balanceReadModel)

        val properties = Properties().apply {
            put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServersConfig)
            put(StreamsConfig.APPLICATION_ID_CONFIG, config.applicationId)
            put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
            put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde::class.java)
            put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, config.schemaRegistryUrlConfig)
            put("value.subject.name.strategy", TopicRecordNameStrategy::class.java)
        }

        return KafkaStreams(builder.build(), properties)
    }

    private fun StreamsBuilder.addBalanceStateStore(config: KafkaConfig): StreamsBuilder =
        addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore(config.stateStores.balanceReadModel),
                Serdes.String(),
                SpecificAvroSerde<BalanceState>().apply {
                    configure(mapOf(SCHEMA_REGISTRY_URL_CONFIG to config.schemaRegistryUrlConfig), false)
                }
            )
        )

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
