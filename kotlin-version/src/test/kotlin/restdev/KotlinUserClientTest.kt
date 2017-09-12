package restdev

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.junit.ErsatzServerRule
import org.junit.Rule
import org.junit.Test
import org.omg.CORBA.Object
import java.util.stream.Collectors

class KotlinUserClientTest {

    @Suppress("UNCHECKED_CAST")
    @get:Rule
    var server: ErsatzServerRule = ErsatzServerRule { config ->
        config.encoder(ContentType.APPLICATION_JSON, KotlinUser::class.java, Encoders.getJson())

        config.encoder(ContentType.APPLICATION_JSON, List::class.java, { o ->
            val users = o as Collection<KotlinUser>
            "[${users.stream().map { u: KotlinUser -> u.toJson() }.collect(Collectors.joining(", "))}]"
        })

        config.decoder(ContentType.APPLICATION_JSON) { bytes, ctx ->
            KotlinUser.fromJson(Decoders.getParseJson().apply(bytes, ctx) as Map<String, Object>)
        }
    }

    @Test
    fun retrieveAll() {
        val users: List<KotlinUser> = listOf(
                KotlinUser(100L, "abe", "abe@example.com"),
                KotlinUser(200L, "bob", "bob@example.com"),
                KotlinUser(300L, "chuck", "chuck@example.com")
        )

        server.expectations { expects ->
            expects.get("/users").called(1).responder { response ->
                response.code(200)
                response.content(users, ContentType.APPLICATION_JSON)
            }
        }

        val client = KotlinUserClient(server.httpUrl)

        val result: List<KotlinUser> = client.retrieveAll()

        assert(result.size == 3)
        assert(result.get(0) == users.get(0))
        assert(result.get(1) == users.get(1))
        assert(result.get(2) == users.get(2))

        assert(server.verify())
    }
}
