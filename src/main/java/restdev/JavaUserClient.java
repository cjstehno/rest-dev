package restdev;

import groovyx.net.http.HttpBuilder;
import groovyx.net.http.NativeHandlers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static groovyx.net.http.ContentTypes.JSON;
import static groovyx.net.http.NativeHandlers.Parsers.json;

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
}
