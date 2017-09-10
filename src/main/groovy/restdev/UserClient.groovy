package restdev

import groovyx.net.http.ChainedHttpConfig
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import groovyx.net.http.NativeHandlers

import static groovyx.net.http.ContentTypes.JSON
import static groovyx.net.http.NativeHandlers.Parsers.json

class UserClient {

    private final HttpBuilder http

    UserClient(final String host) {
        http = HttpBuilder.configure {
            request.uri = host
        }
    }

    // GET /users - list all users, responds with list of users
    List<User> retrieveAll() {
        http.get(List) {
            request.uri.path = '/users'
            request.accept = JSON
            response.parser(JSON) { ChainedHttpConfig config, FromServer fs ->
                json(config, fs).collect { x -> x as User }
            }
        }
    }

    // GET /users/{id} - get specific user, responds with single user
    User retrieve(final long userId) {
        http.get(User) {
            request.uri.path = "/users/${userId}"
            request.accept = JSON
            response.parser JSON, UserClient.&parseJson
        }
    }

    // POST /users <user> - create new user, responds with created user
    User create(final User user) {
        http.post(User) {
            request.uri.path = '/users'
            request.accept = JSON
            request.body = user
            request.contentType = JSON[0]
            request.encoder(JSON, NativeHandlers.Encoders.&json)
            response.parser JSON, UserClient.&parseJson
        }
    }

    // PUT /users/{id} <user> - update existing user, responds with updated user
    User update(final User user) {
        http.put(User) {
            request.uri.path = "/users/${user.id}"
            request.accept = JSON
            request.body = user
            request.contentType = JSON[0]
            request.encoder(JSON, NativeHandlers.Encoders.&json)
            response.parser JSON, UserClient.&parseJson
        }
    }

    // DELETE /users/{id} - delete a user, 200 means success
    boolean delete(final long userId) {
        http.delete {
            request.uri.path = "/users/$userId"
            response.success {
                true
            }
            response.failure {
                throw new IllegalArgumentException()
            }
        }
    }

    private static final User parseJson(ChainedHttpConfig config, FromServer fs) {
        json(config, fs) as User
    }
}
