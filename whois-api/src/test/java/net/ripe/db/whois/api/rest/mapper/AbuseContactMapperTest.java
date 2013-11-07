package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AbuseContactMapperTest {

    @Test
    public void map_abuse_contact() throws XMLStreamException {
        final AbuseResources result = new AbuseContactMapper().mapAbuseContact(
                "AS333",
                Lists.newArrayList(
                        new RpslAttribute("aut-num", "AS333"),
                        new RpslAttribute("abuse-mailbox", "abuse@net.net")));

        assertThat(result.getAbuseContact().getEmail(), is("abuse@net.net"));
        assertThat(result.getLink().getHref(), is("http://rest.db.ripe.net/abuse-contact/AS333"));
        assertThat(result.getParameters().getPrimaryKey().getValue(), is("AS333"));
        assertThat(result.getService(), is("abuse-contact"));
    }
}
