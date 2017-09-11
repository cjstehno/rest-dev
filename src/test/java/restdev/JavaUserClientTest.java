package restdev;

import com.stehno.ersatz.Encoders;
import com.stehno.ersatz.junit.ErsatzServerRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.stehno.ersatz.ContentType.APPLICATION_JSON;
import static com.stehno.ersatz.Decoders.getParseJson;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static restdev.JavaUser.fromJson;

public class JavaUserClientTest {

    @Rule
    @SuppressWarnings("unchecked")
    public ErsatzServerRule server = new ErsatzServerRule(config -> {
        config.encoder(APPLICATION_JSON, JavaUser.class, Encoders.getJson());
        config.encoder(APPLICATION_JSON, List.class, o -> {
            Collection<JavaUser> users = (Collection<JavaUser>) o;
            return "[" + users.stream().map(JavaUser::toJson).collect(joining(", ")) + "]";
        });

        config.decoder(APPLICATION_JSON, (bytes, ctx) -> fromJson((Map<String, Object>) getParseJson().apply(bytes, ctx)));
    });

    @Test
    public void retrieveAll() {
        List<JavaUser> users = Arrays.asList(
            new JavaUser(100L, "abe", "abe@example.com"),
            new JavaUser(200L, "bob", "bob@example.com"),
            new JavaUser(300L, "chuck", "chuck@example.com")
        );

        server.expectations(expects -> expects.get("/users").called(1).responder(response -> {
            response.code(200);
            response.content(users, APPLICATION_JSON);
        }));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        List<JavaUser> result = client.retrieveAll();

        assertEquals(result.size(), 3);
        assertEquals(result.get(0), users.get(0));
        assertEquals(result.get(1), users.get(1));
        assertEquals(result.get(2), users.get(2));

        server.verify();
    }
}
