package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.ClaimException;
import net.ripe.db.whois.update.autokey.NicHandleFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectLoaderTest {
    @Mock NicHandleFactory nicHandleFactory;

    @InjectMocks ObjectLoader subject;

    @Test
    public void nic_hdl_is_auto1() {
        final RpslObject object = RpslObject.parse("person: test person\nnic-hdl: AUTO-1");

        try {
            subject.checkForReservedNicHandle(object);
            fail();
        } catch (ClaimException e) {
            assertThat(e.getErrorMessage().getText(), is("AUTO-1 in nic-hdl is not available in Bootstrap/LoadDump mode"));
        }
    }

    @Test
    public void nic_hdl_is_reserved() throws ClaimException {
        when(nicHandleFactory.isAvailable("TR1-TEST")).thenReturn(false);

        final RpslObject object = RpslObject.parse("role: test role\nnic-hdl: TR1-TEST");
        try {
            subject.checkForReservedNicHandle(object);
            fail();
        } catch (ClaimException e) {
            assertThat(e.getErrorMessage().getFormattedText(), is("The nic-hdl \"TR1-TEST\" is not available"));
        }
    }

    @Test
    public void no_check_for_non_person_role() throws ClaimException {

        final RpslObject object = RpslObject.parse("mntner: test-mnt");
        subject.checkForReservedNicHandle(object);

        verifyZeroInteractions(nicHandleFactory);
    }
}
