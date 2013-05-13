package net.ripe.db.whois.update.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OrganisationIdTest {
    @Test
    public void accessors() {
        final OrganisationId subject = new OrganisationId("SAT", 1, "RIPE");

        assertThat(subject.getSpace(), is("SAT"));
        assertThat(subject.getIndex(), is(1));
        assertThat(subject.getSuffix(), is("RIPE"));
    }

    @Test
    public void string() {
        final OrganisationId subject = new OrganisationId("SAT", 1, "RIPE");
        assertThat(subject.toString(), is("ORG-SAT1-RIPE"));
    }
}
