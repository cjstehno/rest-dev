package restdev

import com.stehno.ersatz.Decoders
import com.stehno.ersatz.DecodingContext
import com.stehno.ersatz.Encoders
import com.stehno.ersatz.ErsatzServer
import spock.lang.AutoCleanup
import spock.lang.Specification

import static com.stehno.ersatz.ContentType.APPLICATION_JSON
import static groovy.json.JsonOutput.toJson

class UserClientSpec extends Specification {

    @AutoCleanup('stop')
    private final ErsatzServer server = new ErsatzServer({
        encoder APPLICATION_JSON, User, Encoders.json
        encoder(APPLICATION_JSON, List) { input ->
            "[${input.collect { i -> toJson(i) }.join(', ')}]"
        }

        decoder(APPLICATION_JSON) { byte[] bytes, DecodingContext dc ->
            Decoders.parseJson.apply(bytes, dc) as User
        }
    })

    def 'retrieveAll'() {
        setup:
        List<User> users = [
            new User(100, 'abe', 'abe@example.com'),
            new User(200, 'bob', 'bob@example.com'),
            new User(300, 'chuck', 'chuck@example.com')
        ]

        server.expectations {
            get('/users').called(1).responder {
                code 200
                content users, APPLICATION_JSON
            }
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        List<User> result = client.retrieveAll()

        then:
        result.size() == 3
        result[0] == users[0]

        and:
        server.verify()
    }

    def 'retrieve'() {
        setup:
        User user = new User(42, 'somebody', 'somebody@example.com')

        server.expectations {
            get('/users/42').called(1).responder {
                code 200
                content user, APPLICATION_JSON
            }
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        User result = client.retrieve(42)

        then:
        result == user

        and:
        server.verify()
    }

    def 'create'() {
        setup:
        User inputUser = new User(null, 'somebody', 'somebody@example.com')
        User createdUser = new User(42, inputUser.username, inputUser.email)

        server.expectations {
            post('/users') {
                called 1
                body inputUser, APPLICATION_JSON
                responder {
                    code 200
                    content createdUser, APPLICATION_JSON
                }
            }
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        User result = client.create(inputUser)

        then:
        result == createdUser

        and:
        server.verify()
    }

    def 'update'() {
        setup:
        User inputUser = new User(42, 'somebody', 'somebody@example.com')
        User updatedUser = new User(42, inputUser.username, 'other@example.com')

        server.expectations {
            put('/users/42') {
                called 1
                body inputUser, APPLICATION_JSON
                responder {
                    code 200
                    content updatedUser, APPLICATION_JSON
                }
            }
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        User result = client.update(inputUser)

        then:
        result == updatedUser

        and:
        server.verify()
    }

    def 'delete: successful'() {
        setup:
        server.expectations {
            delete('/users/42').called(1).responds().code(200)
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        boolean result = client.delete(42)

        then:
        result

        and:
        server.verify()
    }

    def 'delete: failed'() {
        setup:
        server.expectations {
            delete('/users/42').called(1).responds().code(500)
        }

        UserClient client = new UserClient(server.httpUrl)

        when:
        boolean result = client.delete(42)

        then:
        thrown(IllegalArgumentException)
        !result

        and:
        server.verify()
    }
}
