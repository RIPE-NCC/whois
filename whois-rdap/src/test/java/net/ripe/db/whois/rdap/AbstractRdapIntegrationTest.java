package net.ripe.db.whois.rdap;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Redaction;
import net.ripe.db.whois.api.rest.client.RestClientUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.TEXT;
import static net.ripe.db.whois.common.rpsl.AttributeType.E_MAIL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public abstract class AbstractRdapIntegrationTest extends AbstractIntegrationTest {

    private static final String REDACTED_EMAIL_DESCRIPTION = "Personal e-mail information";
    @BeforeAll
    public static void rdapSetProperties() {
        System.setProperty("rdap.sources", "TEST-GRS");
        System.setProperty("rdap.redirect.test", "https://rdap.test.net");
        System.setProperty("rdap.public.baseUrl", "https://rdap.db.ripe.net");
    }

    @AfterAll
    public static void rdapClearProperties() {
        System.clearProperty("rdap.sources");
        System.clearProperty("rdap.redirect.test");
        System.clearProperty("rdap.public.baseUrl");
    }

    // helper methods

    protected WebTarget createResource(final String path) {
        return RestTest.target(getPort(), String.format("rdap/%s", path));
    }

    protected String syncupdate(String data) {
        WebTarget resource = RestTest.target(getPort(), String.format("whois/syncupdates/test"));
        return resource.request()
                .post(jakarta.ws.rs.client.Entity.entity("DATA=" + RestClientUtils.encode(data),
                        MediaType.APPLICATION_FORM_URLENCODED),
                        String.class);

    }

    protected void assertErrorDescription(final WebApplicationException exception, final String description) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getDescription().get(0), is(description));
    }

    protected void assertErrorDescriptionContains(final WebApplicationException exception, final String description) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getDescription().get(0), containsString(description));
    }
    protected void assertErrorTitle(final WebApplicationException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), is(title));
    }

    protected void assertErrorTitleContains(final ClientErrorException exception, final String title) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorTitle(), containsString(title));
    }

    protected void assertErrorStatus(final WebApplicationException exception, final int status) {
        final Entity entity = exception.getResponse().readEntity(Entity.class);
        assertThat(entity.getErrorCode(), is(status));
    }

    protected void assertEmailRedactionForEntities(final RdapObject entity, final List<Entity> entities, final String prefix, final String personKey) {
        final String entityJson = getEntityJson(entity);

        final Redaction redaction = entity.getRedacted().stream()
                .filter(redact -> redact.getPrePath().contains(prefix) && redact.getPrePath().contains(personKey) && redact.getPrePath().contains(E_MAIL.getName()))
                .findAny().get();

        final List<Object> vcards = JsonPath.read(entityJson, redaction.getPrePath());
        assertThat(vcards.size(), is(0));

        final Entity insideEntity = entities.stream().filter(contacEntity -> contacEntity.getHandle().equals(personKey)).findFirst().get();
        assertCommonEmailRedaction(entity, redaction, insideEntity);
    }

    protected String getEntityJson(RdapObject rdapObject) {
        try {
            return new RdapJsonProvider().locateMapper(RdapObject.class, MediaType.APPLICATION_JSON_TYPE).writeValueAsString(rdapObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertCommonEmailRedaction(final RdapObject entity, final Redaction redaction, final Entity insideEntity) {
        ((ArrayList) insideEntity.getVCardArray().get(1)).add(0, Lists.newArrayList(E_MAIL.getName(), "", TEXT.getValue(), "abc@ripe.net"));

        final String entityAfterAddingVcard = getEntityJson(entity);

        final List<Object> vcards = JsonPath.read(entityAfterAddingVcard, redaction.getPrePath());
        assertThat(vcards.size(), is(1));

        ((ArrayList) insideEntity.getVCardArray().get(1)).remove(0);

        assertThat(redaction.getName().getDescription(), is(REDACTED_EMAIL_DESCRIPTION));
        assertThat(redaction.getReason().getDescription(), is("Personal data"));
        assertThat(redaction.getMethod(), is("removal"));
    }


}
