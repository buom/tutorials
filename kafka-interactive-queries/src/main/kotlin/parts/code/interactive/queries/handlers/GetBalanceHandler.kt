package parts.code.interactive.queries.handlers

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.slf4j.LoggerFactory
import parts.code.interactive.queries.config.KafkaConfig
import parts.code.interactive.queries.schemas.BalanceState
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.http.Status
import ratpack.http.client.HttpClient
import ratpack.jackson.Jackson
import java.math.BigDecimal
import java.net.URI
import javax.inject.Inject

class GetBalanceHandler @Inject constructor(
    private val config: KafkaConfig,
    private val streams: KafkaStreams,
    private val currentHostInfo: HostInfo
) : Handler {

    private val logger = LoggerFactory.getLogger(AddFundsHandler::class.java)

    override fun handle(ctx: Context) {
        val customerId = ctx.request.queryParams["customerId"]
        val metadata = streams.metadataForKey(config.stateStores.balanceReadModel, customerId, StringSerializer())
        val hostInfo = metadata.hostInfo()

        if (hostInfo == currentHostInfo) {
            logger.info("Reading local state store on ${hostInfo.toUrl()}")

            val store = streams.store(
                config.stateStores.balanceReadModel,
                QueryableStoreTypes.keyValueStore<String, BalanceState>()
            )

            ctx.response.status(Status.OK)
            ctx.render(Jackson.json(BalancePayload(store.get(customerId).amount)))
        } else {
            logger.info("Proxy to remote state store on ${hostInfo.toUrl()}")

            val httpClient: HttpClient = ctx.get(HttpClient::class.java)
            val uri = URI.create("http://${hostInfo.toUrl()}/api/customers.getBalance?customerId=$customerId")

            httpClient.get(uri).then { response ->
                ctx.response.status(Status.OK)
                ctx.render(response.body.text)
            }
        }
    }

    private data class BalancePayload(val amount: BigDecimal)
    private fun HostInfo.toUrl() = "${host()}:${port()}"
}
