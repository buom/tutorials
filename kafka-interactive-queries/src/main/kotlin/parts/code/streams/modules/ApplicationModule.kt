package parts.code.streams.modules

import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
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
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.Stores
import parts.code.interactive.queries.schemas.BalanceState
import parts.code.streams.config.KafkaConfig
import parts.code.streams.handlers.AddFundsHandler
import parts.code.streams.handlers.GetBalanceHandler
import parts.code.streams.services.KafkaStreamService
import parts.code.streams.suppliers.BalanceProcessor
import ratpack.server.ServerConfig

class ApplicationModule : AbstractModule() {

    override fun configure() {
        bind(KafkaStreamService::class.java)
        bind(AddFundsHandler::class.java)
        bind(GetBalanceHandler::class.java)
    }

    @Provides
    @Singleton
    fun provideKafkaStreams(config: KafkaConfig, hostInfo: HostInfo): KafkaStreams {
        val builder = StreamsBuilder()

        builder
            .addBalanceStateStore(config)
            .stream<String, SpecificRecord>(config.topics.balance)
            .process(ProcessorSupplier { BalanceProcessor(config) }, config.stateStores.balanceReadModel)

        val topology = builder.build()

        val properties = Properties()

        properties[StreamsConfig.APPLICATION_ID_CONFIG] = config.applicationId
        properties[StreamsConfig.APPLICATION_SERVER_CONFIG] = "${hostInfo.host()}:${hostInfo.port()}"
        properties[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java
        properties[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        properties[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = config.bootstrapServersConfig
        properties[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = config.schemaRegistryUrlConfig
        properties["value.subject.name.strategy"] = TopicRecordNameStrategy::class.java

        return KafkaStreams(topology, properties)
    }

    @Provides
    @Singleton
    fun provideHostInfo(serverConfig: ServerConfig): HostInfo = HostInfo("localhost", serverConfig.port)

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
