package net.ripe.db.whois.api.rest.mapper;

import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.attrs.toggles.ChangedAttrFeatureToggle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirtyServerIncomingAttributeMapperTest {

    private static final String BASE_URL = "http://rest-prepdev.db.ripe.net/lookup";

    @Mock
    private ReferencedTypeResolver referencedTypeResolver;

    private DirtyServerIncomingAttributeMapper subject;

    @Before
    public void setup() {
        subject = new DirtyServerIncomingAttributeMapper(referencedTypeResolver, BASE_URL);
    }

    @Test
    public void mapAttribute_drop_changed() {
        when(referencedTypeResolver.getReferencedType(AttributeType.NIC_HDL, CIString.ciString("TP-TEST"))).thenReturn(AttributeType.ROLE.getName());

        final Collection<Attribute> attributes = subject.map(
                new RpslAttribute(AttributeType.CHANGED, CIString.ciString("bitbucket@ripe.net")),
                "TEST");

        assertThat(attributes, hasSize(1));
    }
}