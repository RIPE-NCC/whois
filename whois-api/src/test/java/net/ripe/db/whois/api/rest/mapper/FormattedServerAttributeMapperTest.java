package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.SourceResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FormattedServerAttributeMapperTest {

    private static final String BASE_URL = "http://localhost/lookup";

    @Mock
    private ReferencedTypeResolver referencedTypeResolver;
    @Mock
    private SourceResolver sourceResolver;

    private FormattedServerAttributeMapper subject;

    @BeforeEach
    public void setup() {
        subject = new FormattedServerAttributeMapper(referencedTypeResolver, sourceResolver, BASE_URL);
        lenient().when(sourceResolver.getSource(anyString(), any(CIString.class), anyString())).thenReturn("TEST");
    }

    @Test
    public void map_rpslAttribute_lacking_attributeType() {
        final Collection<Attribute> attributes = subject.map(
                new RpslAttribute("key", CIString.ciString("value")),
                "TEST");

        assertThat(attributes, hasSize(1));

        final Attribute attribute = attributes.iterator().next();

        assertThat(attribute.getLink(), is(nullValue()));
        assertThat(attribute.getValue(), is("value"));
        assertThat(attribute.getName(), is("key"));
    }

    @Test
    public void map_rpslAttribute_attributeType_given() {
        when(referencedTypeResolver.getReferencedType(AttributeType.NIC_HDL, CIString.ciString("TP-TEST"))).thenReturn(AttributeType.ROLE.getName());

        final Collection<Attribute> attributes = subject.map(
                new RpslAttribute(AttributeType.NIC_HDL, CIString.ciString("TP-TEST")),
                "TEST");

        assertThat(attributes, hasSize(1));

        final Attribute attribute = attributes.iterator().next();

        assertThat(attribute.getLink().toString(), is("locator: http://localhost/lookup/TEST/role/TP-TEST"));
        assertThat(attribute.getName(), is("nic-hdl"));
        assertThat(attribute.getValue(), is("TP-TEST"));
    }

    @Test
    public void map_rpslAttribute_multiple_values() {
        when(referencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS1"))).thenReturn(AttributeType.AUT_NUM.getName());
        when(referencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS2"))).thenReturn(AttributeType.AUT_NUM.getName());
        when(referencedTypeResolver.getReferencedType(AttributeType.MEMBERS, CIString.ciString("AS3"))).thenReturn(AttributeType.AUT_NUM.getName());

        final Collection<Attribute> attributes = subject.map(new RpslAttribute(AttributeType.MEMBERS, "AS1, AS2,AS3"), "TEST");

        assertThat(attributes, contains(
            new Attribute("members", "AS1", null, "aut-num", new Link("locator", "http://localhost/lookup/TEST/aut-num/AS1"), null),
            new Attribute("members", "AS2", null, "aut-num", new Link("locator", "http://localhost/lookup/TEST/aut-num/AS2"), null),
            new Attribute("members", "AS3", null, "aut-num", new Link("locator", "http://localhost/lookup/TEST/aut-num/AS3"), null)
        ));
    }

    @Test
    public void map_rpslAttribute_mntRoutes_prefix() {
        when(referencedTypeResolver.getReferencedType(AttributeType.MNT_ROUTES, CIString.ciString("OWNER-MNT {10.0.0.0/8}"))).thenReturn(AttributeType.MNTNER.getName());

        final Collection<Attribute> attributes = subject.map(new RpslAttribute(AttributeType.MNT_ROUTES, "OWNER-MNT {10.0.0.0/8}"), "TEST");

        assertThat(attributes, contains(
            new Attribute("mnt-routes", "OWNER-MNT {10.0.0.0/8}", null, "mntner", new Link("locator", "http://localhost/lookup/TEST/mntner/OWNER-MNT"), null)));
    }
}
