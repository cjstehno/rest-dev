package restdev;

import groovyx.net.http.HttpBuilder;
import groovyx.net.http.NativeHandlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static groovyx.net.http.ContentTypes.JSON;
import static groovyx.net.http.NativeHandlers.Parsers.json;
import static java.lang.String.format;

public class JavaUserClient {

    private final HttpBuilder http;

    @SuppressWarnings("unchecked")
    public JavaUserClient(final String host) {
        http = HttpBuilder.configure(config -> {
            config.getRequest().setUri(host);
            config.getRequest().encoder(JSON, NativeHandlers.Encoders::json);
            config.getResponse().parser(JSON, (chained, fs) -> JavaUser.fromJson((Map<String, Object>) json(chained, fs)));
        });
    }

    // GET /users - list all users, responds with list of users
    @SuppressWarnings("unchecked")
    public List<JavaUser> retrieveAll() {
        return http.get(List.class, config -> {
            config.getRequest().getUri().setPath("/users");
            config.getResponse().parser(JSON, (chained, fs) -> {
                List<Map<String, Object>> json = (List<Map<String, Object>>) json(chained, fs);
                return json.stream().map((Function<Map<String, Object>, Object>) JavaUser::fromJson).collect(Collectors.toList());
            });
        });
    }

    public JavaUser retrieve(final long userId) {
        return http.get(JavaUser.class, config -> config.getRequest().getUri().setPath(format("/users/%d", userId)));
    }

    // POST /users <user> - create new user, responds with created user
    public JavaUser create(final JavaUser user) {
        return http.post(JavaUser.class, config -> {
            config.getRequest().getUri().setPath("/users");
            config.getRequest().setBody(user);
            config.getRequest().setContentType(JSON.getAt(0));
        });
    }

    // PUT /users/{id} <user> - update existing user, responds with updated user
    public JavaUser update(final JavaUser user) {
        return http.put(JavaUser.class, config -> {
            config.getRequest().getUri().setPath("/users/" + user.getId());
            config.getRequest().setBody(user);
            config.getRequest().setContentType(JSON.getAt(0));
        });
    }

    // DELETE /users/{id} - delete a user, 200 means success
    public boolean delete(final long userId) {
        return http.delete(Boolean.class, config -> {
            config.getRequest().getUri().setPath("/users/" + userId);
            config.getResponse().success((fromServer, o) -> true);
            config.getResponse().failure((fromServer, o) -> {
                throw new IllegalArgumentException();
            });
        });
    }
}
