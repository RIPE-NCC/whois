package net.ripe.db.whois.update.handler.validator.sets;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PeeringSetAttributeMustBePresentTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    private PeeringSetAttributeMustBePresent subject;

    @Before
    public void setup() {
        subject = new PeeringSetAttributeMustBePresent();

    }

    @Test
    public void testGetActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void testGetTypes() {
        assertThat(subject.getTypes(), Matchers.contains(ObjectType.PEERING_SET, ObjectType.FILTER_SET));
    }

    @Test
    public void mpPeering_and_peering_present() {
        when(update.getType()).thenReturn(ObjectType.PEERING_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("peering-set: prng-ripe\ndescr: description\npeering: AS6845 at 194.102.255.254\nmp-peering: AS702:PRNG-AT-CUSTOMER"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
    }

    @Test
    public void mpPeering_nor_peering_present() {
        when(update.getType()).thenReturn(ObjectType.PEERING_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("peering-set: prng-ripe\ndescr: description"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
    }

    @Test
    public void only_mpPeering_present() {
        when(update.getType()).thenReturn(ObjectType.PEERING_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("peering-set: prng-ripe\ndescr: description\nmp-peering: AS702:PRNG-AT-CUSTOMER"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
        verify(updateContext, never()).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
    }

    @Test
    public void only_peering_present() {
        when(update.getType()).thenReturn(ObjectType.PEERING_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("peering-set: prng-ripe\ndescr: description\npeering: AS6845 at 194.102.255.254"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
        verify(updateContext, never()).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.PEERING_SET, "peering", "mp-peering"));
    }


    @Test
    public void filter_and_mpFilter_present() {
        when(update.getType()).thenReturn(ObjectType.FILTER_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: prng-ripe\ndescr: description\nfilter: AS6845 at 194.102.255.254\nmp-filter: AS702:PRNG-AT-CUSTOMER"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
    }

    @Test
    public void mpFilter_nor_filter_present() {
        when(update.getType()).thenReturn(ObjectType.FILTER_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: prng-ripe\ndescr: description"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
    }

    @Test
    public void only_mpFilter_present() {
        when(update.getType()).thenReturn(ObjectType.FILTER_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: prng-ripe\ndescr: description\nmp-filter: AS702:PRNG-AT-CUSTOMER"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
        verify(updateContext, never()).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
    }

    @Test
    public void only_filter_present() {
        when(update.getType()).thenReturn(ObjectType.FILTER_SET);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("filter-set: prng-ripe\ndescr: description\nfilter: AS6845 at 194.102.255.254"));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.eitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
        verify(updateContext, never()).addMessage(update, UpdateMessages.neitherSimpleOrComplex(ObjectType.FILTER_SET, "filter", "mp-filter"));
    }
}