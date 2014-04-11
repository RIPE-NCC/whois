package net.ripe.db.whois.update.generator;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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

import static org.hamcrest.Matchers.hasSize;
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
        assertThat(result.getAttributes().get(2).getType(), is(AttributeType.STATUS));
    }

    @Test
    public void generate_legacy_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");
        isMaintainedByRir(true);
        when(legacyAutnum.contains(any(CIString.class))).thenReturn(Boolean.TRUE);

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
    public void generate_moves_remarks_before_status() {
        final RpslObject autnum = RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n"
        );

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getAttributes(), hasSize(5));
        assertThat(result.getAttributes().get(1), is(AutnumAttributeGenerator.STATUS_REMARK));
        assertThat(result.getAttributes().get(2), is(new RpslAttribute(AttributeType.STATUS, "OTHER")));
    }

    @Test
    public void generate_leaves_other_remarks_lines_alone() {
        final RpslObject autnum = RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "mnt-by: TEST-MNT\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "source: RIPE\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n"
        );

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result, is(RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n"
        )));
    }

    @Test
    public void changed_remarks_is_readded() {
        final RpslObject autnum = RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "source: RIPE\n"
        );

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result, is(RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n"
        )));
    }

    @Test
    public void appended_remarks_is_readded() {
        final RpslObject autnum = RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + " <-- line added by real ninjas, not me\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "source: RIPE\n"
        );

        final RpslObject result = autnumStatusAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result, is(RpslObject.parse("" +
                        "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + " <-- line added by real ninjas, not me\n" +
                        "remarks: " + AutnumAttributeGenerator.REMARKS_TEXT + "\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n"
        )));
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
