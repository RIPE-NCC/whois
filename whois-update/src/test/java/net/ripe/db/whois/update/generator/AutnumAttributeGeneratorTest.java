package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.LegacyAutnum;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutnumAttributeGeneratorTest {

    @Mock Update update;
    @Mock UpdateContext updateContext;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock AuthoritativeResource authoritativeResource;
    @Mock SourceContext sourceContext;
    @Mock LegacyAutnum legacyAutnum;
    @InjectMocks AutnumAttributeGenerator autnumStatusAttributeGenerator;

    @Before
    public void setup() {
        when(sourceContext.getCurrentSource()).thenReturn(Source.master("TEST"));
        when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        isMaintainedByRir(false);
        when(legacyAutnum.contains(any(CIString.class))).thenReturn(Boolean.FALSE);
    }

    @Test
    public void generate_assigned_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");
        isMaintainedByRir(true);
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("ASSIGNED")));
        assertThat(result.getAttributes().get(1).getType(), is(AttributeType.STATUS));
    }

    @Test
    public void generate_legacy_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");
        isMaintainedByRir(true);
        when(legacyAutnum.contains(any(CIString.class))).thenReturn(Boolean.TRUE);
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("LEGACY")));
    }

    @Test
    public void generate_other_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");

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
        when(authoritativeResource.isMaintainedInRirSpace(any(ObjectType.class), any(CIString.class))).thenReturn(maintained);
    }
}
