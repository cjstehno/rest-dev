package restdev

import groovyx.net.http.ContentTypes
import groovyx.net.http.HttpBuilder
import groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.NativeHandlers.Encoders
import groovyx.net.http.NativeHandlers.Parsers
import java.util.function.Consumer
import java.util.stream.Collectors

@Suppress("UNCHECKED_CAST")
class KotlinUserClient(val host: String) {


    val http: HttpBuilder = configure(Consumer { config ->
        config.request.setUri(host)

        config.request.encoder(ContentTypes.JSON, Encoders::json)

        config.response.parser(ContentTypes.JSON, { chained, fs ->
            KotlinUser.fromJson(Parsers.json(chained, fs) as Map<String, Any>)
        })
    })

    fun retrieveAll(): List<KotlinUser> {
        return http.get(List::class.java) { config ->
            config.request.uri.setPath("/users")

            config.response.parser(ContentTypes.JSON) { chained, fs ->
                val json: List<Map<String, Any>> = Parsers.json(chained, fs) as List<Map<String, Any>>
                json.stream().map { entry: Map<String, Any> -> KotlinUser.fromJson(entry) }.collect(Collectors.toList())
            }
        } as List<KotlinUser>
    }

    fun retrieve(userId: Long): KotlinUser {
        return http.get(KotlinUser::class.java) { config ->
            config.request.uri.setPath("/users/$userId")
        }
    }

    fun create(user: KotlinUser): KotlinUser {
        return http.post(KotlinUser::class.java) { config ->
            config.request.uri.setPath("/users")
            config.request.setBody(user)
            config.request.setContentType(ContentTypes.JSON.getAt(0))
        }
    }

    fun update(user: KotlinUser): KotlinUser {
        return http.put(KotlinUser::class.java) { config ->
            config.request.uri.setPath("/users/${user.id}")
            config.request.setBody(user)
            config.request.setContentType(ContentTypes.JSON.getAt(0))
        }
    }

    fun delete(userId: Long): Boolean {
        return http.delete { config ->
            config.request.uri.setPath("/users/$userId")
            config.response.success { _, _ ->
                true
            }
            config.response.failure { _, _ ->
                throw IllegalArgumentException()
            }
        } as Boolean
    }
}