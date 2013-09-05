package net.ripe.db.whois.query.query;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.Validate;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttributeMatcherTest {

    @Test
    public void searchKeyTypesName() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.PERSON, Query.parse("name")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.PERSON, Query.parse("one-two-three")));
    }

    @Test
    public void searchKeyTypesOrganisationId() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("ORG-AX1-RIPE")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("oRg-aX1-rIPe")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.ORGANISATION, Query.parse("name")));
    }

    @Test
    public void searchKeyTypesNicHandle() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("AA1-DEV")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("aA1-deV")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.NIC_HDL, Query.parse("name")));
    }

    @Test
    public void searchKeyEmail() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("cac37ak@ripe.net")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("person@domain.com")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.E_MAIL, Query.parse("me@some.nl")));
    }

    @Test
    public void searchInetnum() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("10.11.12.0/24")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("10.11.12.0/24")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("10.11.12.0/24")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("10.11.12.0/24")));
    }

    @Test
    public void searchInet6num() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("2001::/32")));
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("2001::/32")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("2001::/32")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("2001::/32")));
    }

    @Test
    public void searchRoute() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("10.11.12.0/24AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("10.11.12.0/24AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("10.11.12.0/24AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("10.11.12.0/24AS3333")));
    }

    @Test
    public void searchRoute6() {
        assertTrue(AttributeMatcher.fetchableBy(AttributeType.ROUTE6, Query.parse("2001::/32AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.ROUTE, Query.parse("2001::/32AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INETNUM, Query.parse("2001::/32AS3333")));
        assertFalse(AttributeMatcher.fetchableBy(AttributeType.INET6NUM, Query.parse("2001::/32AS3333")));
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
