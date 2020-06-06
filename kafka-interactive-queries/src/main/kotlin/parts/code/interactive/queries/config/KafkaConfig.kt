package parts.code.interactive.queries.config

data class KafkaConfig(
    val applicationId: String,
    val bootstrapServersConfig: String,
    val schemaRegistryUrlConfig: String,
    val topics: Topics,
    val stateStores: StateStores
) {

    data class Topics(
        val balance: String
    )

    data class StateStores(
        val balanceReadModel: String
    )
}
