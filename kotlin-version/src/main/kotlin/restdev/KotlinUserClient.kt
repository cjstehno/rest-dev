package restdev

import groovyx.net.http.*
import groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.NativeHandlers.Encoders
import groovyx.net.http.NativeHandlers.Parsers
import java.util.function.Consumer
import java.util.stream.Collectors

class KotlinUserClient(val host: String) {

    val http: HttpBuilder = configure(Consumer { config: HttpObjectConfig ->
        config.request.setUri(host)

        config.request.encoder(ContentTypes.JSON, Encoders::json)

        config.response.parser(ContentTypes.JSON, { chained: ChainedHttpConfig, fs: FromServer ->
            Parsers.json(chained, fs) as KotlinUser
        })
    })

    @Suppress("UNCHECKED_CAST")
    fun retrieveAll(): List<KotlinUser> {
        return http.get(List::class.java) { config: HttpConfig ->
            config.request.uri.setPath("/users")

            config.response.parser(ContentTypes.JSON) { chained: ChainedHttpConfig, fs: FromServer ->
                val json: List<Map<String, Any>> = Parsers.json(chained, fs) as List<Map<String, Any>>
                json.stream().map { entry: Map<String, Any> -> KotlinUser.fromJson(entry) }.collect(Collectors.toList())
            }
        } as List<KotlinUser>
    }
}