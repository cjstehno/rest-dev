package restdev

import com.stehno.ersatz.ContentType
import com.stehno.ersatz.Decoders
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.junit.ErsatzServerRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

        assertEquals(result.size.toLong(), 3)
        assertEquals(result[0], users[0])
        assertEquals(result[1], users[1])
        assertEquals(result[2], users[2])

        assertTrue(server.verify())
    }

    @Test
    fun retrieve() {
        val user = KotlinUser(42L, "somebody", "sombody@example.com")

        server.expectations { expects ->
            expects.get("/users/42").called(1).responder { response ->
                response.code(200)
                response.content(user, ContentType.APPLICATION_JSON)
            }
        }

        val client = KotlinUserClient(server.httpUrl)

        val result: KotlinUser = client.retrieve(user.id!!)

        assertEquals(result, user)

        assertTrue(server.verify())
    }

    @Test
    fun create() {
        val inputUser = KotlinUser(null, "somebody", "somebody@example.com")
        val createdUser = KotlinUser(42L, inputUser.username, inputUser.email)

        server.expectations { expects ->
            expects.post("/users") { request ->
                request.called(1)
                request.body(inputUser, ContentType.APPLICATION_JSON)
                request.responder { response ->
                    response.code(200)
                    response.content(createdUser, ContentType.APPLICATION_JSON)
                }
            }
        }

        val client = KotlinUserClient(server.httpUrl)

        val result: KotlinUser = client.create(inputUser)

        assertEquals(createdUser, result)

        assertTrue(server.verify())
    }

    @Test
    fun update() {
        val inputUser = KotlinUser(42L, "somebody", "somebody@example.com")
        val updatedUser = KotlinUser(inputUser.id, inputUser.username, "other@example.com")

        server.expectations { expects ->
            expects.put("/users/42") { request ->
                request.called(1)
                request.body(inputUser, ContentType.APPLICATION_JSON)
                request.responder { response ->
                    response.code(200)
                    response.content(updatedUser, ContentType.APPLICATION_JSON)
                }
            }
        }

        val client = KotlinUserClient(server.httpUrl)

        val result: KotlinUser = client.update(inputUser)

        assertEquals(updatedUser, result)

        assertTrue(server.verify())
    }

    @Test
    fun successful_delete() {
        server.expectations { expects ->
            expects.delete("/users/42").called(1).responds().code(200)
        }

        val client = KotlinUserClient(server.httpUrl)

        val result: Boolean = client.delete(42L)

        assertTrue(result)

        assertTrue(server.verify())
    }

    @Test(expected = IllegalArgumentException::class)
    fun failed_delete() {
        server.expectations { expects ->
            expects.delete("/users/42").called(1).responds().code(500)
        }

        val client = KotlinUserClient(server.httpUrl)

        client.delete(42L)
    }
}
