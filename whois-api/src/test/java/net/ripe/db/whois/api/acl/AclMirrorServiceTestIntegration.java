package net.ripe.db.whois.api.acl;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AclMirrorServiceTestIntegration extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.INTERNAL;
    private static final String MIRRORS_PATH = "api/acl/mirrors";

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertAclMirror("0/0");
        databaseHelper.insertAclMirror("10.0.0.2/32");
        databaseHelper.insertAclMirror("::0/0");
    }

    @Test
    public void mirrors() {
        databaseHelper.insertAclMirror("10.0.0.0/32");
        databaseHelper.insertAclMirror("10.0.0.1/32");

        List<Mirror> mirrors = getMirrors();

        assertThat(mirrors, hasSize(5));
    }

    @Test
    public void getMirror_non_existing() {
        try {
            createResource(AUDIENCE, MIRRORS_PATH, "10.0.0.0/32")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Mirror.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
        }
    }

    @Test
    public void getMirror_existing() {
        databaseHelper.insertAclMirror("10.0.0.0/32");

        Mirror mirror = createResource(AUDIENCE, MIRRORS_PATH, "10.0.0.0/32")
                .accept(MediaType.APPLICATION_JSON)
                .get(Mirror.class);

        assertThat(mirror.getPrefix(), is("10.0.0.0/32"));
    }

    @Test
    public void getMirror_invalid() {
        try {
            createResource(AUDIENCE, MIRRORS_PATH, "10")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Mirror.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("'10' is not an IP string literal."));
        }
    }

    @Test
    public void createMirror() {
        Mirror mirror = createResource(AUDIENCE, MIRRORS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .post(Mirror.class, new Mirror("10.0.0.0/32", "comment"));

        assertThat(mirror.getPrefix(), is("10.0.0.0/32"));
        assertThat(mirror.getComment(), is("comment"));

        List<Mirror> mirrors = getMirrors();
        assertThat(mirrors, hasSize(4));
    }

    @Test
    public void createMirror_invalid() {
        try {
            createResource(AUDIENCE, MIRRORS_PATH)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(Mirror.class, new Mirror("10", "comment"));
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
            assertThat(e.getResponse().getEntity(String.class), is("'10' is not an IP string literal."));
        }
    }

    @Test
    public void updateMirror() {
        Mirror mirror = createResource(AUDIENCE, MIRRORS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Mirror.class, new Mirror("10.0.0.2/32", "changed comment"));

        assertThat(mirror.getPrefix(), is("10.0.0.2/32"));
        assertThat(mirror.getComment(), is("changed comment"));
        assertThat(getMirrors(), hasSize(3));
    }

    @Test
    public void deleteMirror() throws Exception {
        final Mirror mirror = createResource(AUDIENCE, MIRRORS_PATH, "10.0.0.2/32")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete(Mirror.class);

        assertThat(mirror.getPrefix(), is("10.0.0.2/32"));
        assertThat(getMirrors(), hasSize(2));

    }

    @SuppressWarnings("unchecked")
    private List<Mirror> getMirrors() {
        return createResource(AUDIENCE, MIRRORS_PATH)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<List<Mirror>>() {
                });
    }
}

