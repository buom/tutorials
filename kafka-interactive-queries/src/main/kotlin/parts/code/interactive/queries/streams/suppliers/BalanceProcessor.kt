package parts.code.interactive.queries.streams.suppliers

import javax.inject.Inject
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import parts.code.interactive.queries.config.KafkaConfig
import parts.code.interactive.queries.schemas.BalanceState
import parts.code.interactive.queries.schemas.FundsAdded
import java.math.BigDecimal

class BalanceProcessor @Inject constructor(private val config: KafkaConfig) : Processor<String, SpecificRecord> {

    private val logger = LoggerFactory.getLogger(BalanceProcessor::class.java)
    private lateinit var stateStore: KeyValueStore<String, BalanceState>

    @Suppress("UNCHECKED_CAST")
    override fun init(context: ProcessorContext) {
        stateStore = context.getStateStore(config.stateStores.balanceReadModel) as KeyValueStore<String, BalanceState>
    }

    override fun process(key: String, record: SpecificRecord) {
        if (record is FundsAdded) {
            val newBalance = (stateStore.get(record.customerId)?.amount ?: BigDecimal.ZERO) + record.amount
            stateStore.put(record.customerId, BalanceState(record.customerId, newBalance))

            logger.info("Processed ${record.schema.name}\n\trecord: $record")
        }
    }

    override fun close() {}
}
