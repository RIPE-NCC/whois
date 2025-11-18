package net.ripe.db.whois.api.rest.marshal;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class StreamingMarshalJsonTest {


    @Test
    public void serialise_whois_resources_to_json() {
        final WhoisResources whoisResources = new WhoisResources();
        final WhoisObject person = new WhoisObject();
        person.setAttributes(Lists.newArrayList(new Attribute("person", "test person"), new Attribute("source", "TEST")));
        final WhoisObject role = new WhoisObject();
        role.setAttributes(Lists.newArrayList(new Attribute("role", "test role"), new Attribute("source", "TEST")));
        whoisResources.setWhoisObjects(Lists.newArrayList(person, role));

        assertThat(serialise(whoisResources), containsString(
            "[ {\n" +
                    "  \"attributes\" : {\n" +
                    "    \"attribute\" : [ {\n" +
                    "      \"name\" : \"person\",\n" +
                    "      \"value\" : \"test person\"\n" +
                    "    }, {\n" +
                    "      \"name\" : \"source\",\n" +
                    "      \"value\" : \"TEST\"\n" +
                    "    } ]\n" +
                    "  }\n" +
                    "}, {\n" +
                    "  \"attributes\" : {\n" +
                    "    \"attribute\" : [ {\n" +
                    "      \"name\" : \"role\",\n" +
                    "      \"value\" : \"test role\"\n" +
                    "    }, {\n" +
                    "      \"name\" : \"source\",\n" +
                    "      \"value\" : \"TEST\"\n" +
                    "    } ]\n" +
                    "  }\n" +
                    "} ]"));
    }

    private static String serialise(final WhoisResources whoisResources) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final StreamingMarshalJson streamingMarshalJson = new StreamingMarshalJson(baos);
        streamingMarshalJson.writeArray(whoisResources.getWhoisObjects());
        return new String(baos.toByteArray());
    }

}
