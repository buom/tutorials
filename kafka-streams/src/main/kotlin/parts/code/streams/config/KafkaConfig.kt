package parts.code.streams.config

data class KafkaConfig(
    val applicationId: String,
    val bootstrapServersConfig: String,
    val schemaRegistryUrlConfig: String,
    val topics: Topics
) {

    data class Topics(
        val preferencesAuthorization: String,
        val preferences: String
    )
}
