package restdev;

import java.util.Map;

import static java.lang.Long.parseLong;
import static java.lang.String.format;

public class JavaUser {

    private Long id;
    private String username;
    private String email;

    public JavaUser() {
        this(null, null, null);
    }

    public JavaUser(final Long id, final String username, final String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JavaUser javaUser = (JavaUser) o;

        if (id != null ? !id.equals(javaUser.id) : javaUser.id != null) return false;
        if (username != null ? !username.equals(javaUser.username) : javaUser.username != null) return false;
        return email != null ? email.equals(javaUser.email) : javaUser.email == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return format("JavaUser{id=%d, username='%s', email='%s'}", id, username, email);
    }

    public String toJson() {
        return format("{\"id\":%d, \"username\":\"%s\", \"email\":\"%s\"}", id, username, email);
    }

    static JavaUser fromJson(final Map<String, Object> json) {
        Object id = json.get("id");
        return new JavaUser(id != null ? parseLong(id.toString()) : null, (String) json.get("username"), (String) json.get("email"));
    }
}
