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
import static org.junit.Assert.assertTrue;
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

        assertTrue(server.verify());
    }

    @Test
    public void retrieve() {
        JavaUser user = new JavaUser(42L, "somebody", "somebody@example.com");

        server.expectations(expects -> expects.get("/users/42").called(1).responder(response -> {
            response.code(200);
            response.content(user, APPLICATION_JSON);
        }));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        JavaUser result = client.retrieve(42);

        assertEquals(result, user);

        assertTrue(server.verify());
    }

    @Test
    public void create() {
        JavaUser inputUser = new JavaUser(null, "somebody", "somebody@example.com");
        JavaUser createdUser = new JavaUser(42L, inputUser.getUsername(), inputUser.getEmail());

        server.expectations(expects -> expects.post("/users", request -> {
            request.called(1);
            request.body(inputUser, APPLICATION_JSON);
            request.responder(response -> {
                response.code(200);
                response.content(createdUser, APPLICATION_JSON);
            });
        }));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        JavaUser user = client.create(inputUser);

        assertEquals(user, createdUser);

        assertTrue(server.verify());
    }

    @Test
    public void update() {
        JavaUser inputUser = new JavaUser(42L, "somebody", "somebody@example.com");
        JavaUser updatedUser = new JavaUser(inputUser.getId(), inputUser.getUsername(), "other@example.com");

        server.expectations(expects -> expects.put("/users/42", request -> {
            request.called(1);
            request.body(inputUser, APPLICATION_JSON);
            request.responder(response -> {
                response.code(200);
                response.content(updatedUser, APPLICATION_JSON);
            });
        }));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        JavaUser user = client.update(inputUser);

        assertEquals(user, updatedUser);

        assertTrue(server.verify());
    }

    @Test
    public void delete_successful() {
        server.expectations(expects -> expects.delete("/users/42").called(1).responds().code(200));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        boolean result = client.delete(42);

        assertTrue(result);

        assertTrue(server.verify());
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_failure() {
        server.expectations(expects -> expects.delete("/users/42").called(1).responds().code(500));

        JavaUserClient client = new JavaUserClient(server.getHttpUrl());

        client.delete(42);
    }
}
