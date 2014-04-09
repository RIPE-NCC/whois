package net.ripe.db.whois.update.generator;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.LegacyAutnum;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutnumAttributeGeneratorTest {

    @Mock Update update;
    @Mock UpdateContext updateContext;
    @Mock Maintainers maintainers;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock AuthoritativeResource authoritativeResource;
    @Mock SourceContext sourceContext;
    @Mock LegacyAutnum legacyAutnum;
    @InjectMocks AutnumAttributeGenerator autnumStatusAttributeGenerator;

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
        when(sourceContext.getCurrentSource()).thenReturn(Source.master("TEST"));
        when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        isMaintainedByRir(false);
        when(legacyAutnum.contains(any(CIString.class))).thenReturn(Boolean.FALSE);
    }

    @Test
    public void generate_assigned_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");
        isMaintainedByRir(true);

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("ASSIGNED")));
    }

    @Test
    public void generate_legacy_status_on_create() {
        final RpslObject autnum  = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");
        isMaintainedByRir(true);
        when(legacyAutnum.contains(any(CIString.class))).thenReturn(Boolean.TRUE);

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("LEGACY")));
    }

    @Test
    public void generate_other_status_on_create() {
        final RpslObject autnum  = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("OTHER")));
    }

    @Test
    public void generate_other_status_on_update() {
        final RpslObject originalObject = RpslObject.parse("aut-num: AS3333\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");
        final RpslObject updatedObject = RpslObject.parse("aut-num: AS3333\nremarks: updated\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(originalObject, updatedObject, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("OTHER")));
    }

    // helper methods

    private void isMaintainedByRir(final boolean maintained) {
        when(authoritativeResource.isMaintainedByRir(any(ObjectType.class), any(CIString.class))).thenReturn(maintained);
    }
}
