package parts.code.interactive.queries

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import parts.code.interactive.queries.handlers.GetBalanceHandler
import parts.code.interactive.queries.config.KafkaConfig
import parts.code.interactive.queries.handlers.AddFundsHandler
import parts.code.interactive.queries.modules.ApplicationModule
import ratpack.guice.Guice
import ratpack.handling.Chain
import ratpack.server.BaseDir
import ratpack.server.RatpackServer
import java.time.Clock

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        RatpackServer.start { server ->
            server
                .serverConfig {
                    it
                        .baseDir(BaseDir.find())
                        .yaml("application.yaml")
                        .require("/kafka", KafkaConfig::class.java)
                        .jacksonModules(KotlinModule())
                }
                .registry(Guice.registry {
                    it
                        .module(ApplicationModule::class.java)
                        .bindInstance(Clock::class.java, Clock.systemUTC())
                        .bindInstance(ObjectMapper::class.java, ObjectMapper().registerModule(KotlinModule()))
                })
                .handlers {
                    it
                        .prefix("api", ::endpoints)
                }
        }
    }

    private fun endpoints(chain: Chain) {
        chain
            .post("balance.addFunds", AddFundsHandler::class.java)
            .get("customers.getBalance", GetBalanceHandler::class.java)
    }
}
