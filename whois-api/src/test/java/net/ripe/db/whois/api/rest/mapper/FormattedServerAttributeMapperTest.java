package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormattedServerAttributeMapperTest {

    private static final String BASE_URL = "http://rest.db.ripe.net/lookup";

    @Mock private ReferencedTypeResolver referencedTypeResolver;

    private FormattedServerAttributeMapper subject;

    @Before
    public void setup(){
        subject = new FormattedServerAttributeMapper(referencedTypeResolver, BASE_URL);
    }

    @Test
    public void mapAttribute_lacking_attributeType() {
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
    public void mapAttribute_attributeType_given() {
        when(referencedTypeResolver.getReferencedType(AttributeType.NIC_HDL, CIString.ciString("TP-TEST"))).thenReturn(AttributeType.ROLE.getName());

        final Collection<Attribute> attributes = subject.map(
                new RpslAttribute(AttributeType.NIC_HDL, CIString.ciString("TP-TEST")),
                "TEST");

        assertThat(attributes, hasSize(1));

        final Attribute attribute = attributes.iterator().next();

        assertThat(attribute.getLink().toString(), is("locator: http://rest.db.ripe.net/lookup/TEST/role/TP-TEST"));
        assertThat(attribute.getName(), is("nic-hdl"));
        assertThat(attribute.getValue(), is("TP-TEST"));
    }
}