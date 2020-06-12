package parts.code.streams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.Clock
import parts.code.streams.config.KafkaConfig
import parts.code.streams.handlers.CreatePreferencesHandler
import parts.code.streams.modules.ApplicationModule
import ratpack.guice.Guice
import ratpack.handling.Chain
import ratpack.server.BaseDir
import ratpack.server.RatpackServer

object StreamProcessingApplication {

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
                        .env()
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
            .post("preferences.create", CreatePreferencesHandler::class.java)
    }
}
