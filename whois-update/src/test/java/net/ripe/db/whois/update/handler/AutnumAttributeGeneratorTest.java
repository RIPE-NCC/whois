package net.ripe.db.whois.update.handler;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutnumAttributeGeneratorTest {

    @Mock Update update;
    @Mock UpdateContext updateContext;
    @Mock Maintainers maintainers;
    @InjectMocks AutnumAttributeGenerator autnumAttributeGenerator;

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
    }

    @Test
    public void generate_other_status_on_create() {
        final RpslObject autnum  = RpslObject.parse("aut-num: AS3333\nmnt-by: TEST-MNT\nsource: RIPE");

        final RpslObject result = autnumAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("OTHER")));
    }

    @Test
    public void generate_assigned_status_on_create() {
        final RpslObject autnum = RpslObject.parse("aut-num: AS3333\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");

        final RpslObject result = autnumAttributeGenerator.generateAttributes(null, autnum, update, updateContext);

        assertThat(result.getValueForAttribute(AttributeType.STATUS), is(CIString.ciString("ASSIGNED")));
    }


    @Test
    public void dont_generate_status_on_update() {
        final RpslObject originalObject = RpslObject.parse("aut-num: AS3333\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");
        final RpslObject updatedObject = RpslObject.parse("aut-num: AS3333\nremarks: updated\nmnt-by: RIPE-NCC-HM-MNT\nsource: RIPE");

        final RpslObject result = autnumAttributeGenerator.generateAttributes(originalObject, updatedObject, update, updateContext);

        assertThat(result.containsAttribute(AttributeType.STATUS), is(false));
    }



}
