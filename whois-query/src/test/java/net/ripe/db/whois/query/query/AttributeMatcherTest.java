package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AttributeMatcherTest {

    @Test
    public void searchKeyTypesName() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.PERSON, Query.parse("name")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.PERSON, Query.parse("one-two-three")), is(true));
    }

    @Test
    public void searchKeyTypesOrganisationId() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("ORG-AX1-RIPE")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("oRg-aX1-rIPe")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("name")), is(false));
    }

    @Test
    public void searchKeyTypesNicHandle() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("AA1-DEV")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("aA1-deV")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("name")), is(true));
    }

    @Test
    public void searchKeyEmail() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("cac37ak@ripe.net")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("person@domain.com")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("me@some.nl")), is(true));
    }

    @Test
    public void searchInetnum() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("10.11.12.0/24")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("10.11.12.0/24")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("10.11.12.0/24")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("10.11.12.0/24")), is(false));
    }

    @Test
    public void searchInet6num() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("2001::/32")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("2001::/32")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("2001::/32")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("2001::/32")), is(false));
    }

    @Test
    public void searchRoute() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("10.11.12.0/24AS3333")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("10.11.12.0/24AS3333")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("10.11.12.0/24AS3333")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("10.11.12.0/24AS3333")), is(false));
    }

    @Test
    public void searchRoute6() {
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("2001::/32AS3333")), is(true));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("2001::/32AS3333")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("2001::/32AS3333")), is(false));
        assertThat(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("2001::/32AS3333")), is(false));
    }

    @Test
    public void checkAllSupported() {
        for (final ObjectType objectType : ObjectType.values()) {
            final ObjectTemplate template = ObjectTemplate.getTemplate(objectType);
            for (final AttributeType lookupAttribute : template.getLookupAttributes()) {
                Validate.isTrue(AttributeMatcher.attributeMatchers.containsKey(lookupAttribute), "No matcher for lookup attribute: " + lookupAttribute + " defined for " + objectType);
            }
        }
    }
}
