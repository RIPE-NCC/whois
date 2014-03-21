package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SponsoringOrgValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock RpslObjectDao objectDao;
    @Mock Maintainers maintainers;

    @InjectMocks SponsoringOrgValidator subject;

    @Test
    public void applies_on_create_modify() {
        assertThat(subject.getActions(), hasItems(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void applies_to_resources() {
        assertThat(subject.getTypes(), hasItems(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM));
    }

    @Test
    public void sponsoring_org_not_changed() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: ORG-MNT\n" +
                "source: TEST"));
        when(update.getAction()).thenReturn(Action.MODIFY);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgChanged());
        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
    }

    public void sponsoring_org_not_added() {
        final RpslObject object = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST");
        when(update.getReferenceObject()).thenReturn(object);
        when(update.getUpdatedObject()).thenReturn(object);
        when(update.getAction()).thenReturn(Action.CREATE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgChanged());
        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
    }

    @Test
    public void rpslobject_has_org_ref_with_status_other_than_lir() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "sponsoring-org: ORG-SP1-TEST\n" +
                "mnt-by: ORG-MNT\n" +
                "source: TEST"));

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(objectDao.getByKey(ObjectType.ORGANISATION, CIString.ciString("ORG-TR1-TEST"))).thenReturn(RpslObject.parse("" +
                "organisation: ORG-TR1-TEST\n" +
                "org-type: OTHER\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
        when(update.isOverride()).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
        verify(updateContext).addMessage(update, UpdateMessages.sponsoringOrgChanged());
    }

    @Test
    public void sponsoringOrg_changed_rsmaintainer() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "sponsoring-org: ORG-SP1-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT\n" +
                "source: TEST"));

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(objectDao.getByKey(ObjectType.ORGANISATION, CIString.ciString("ORG-TR1-TEST"))).thenReturn(RpslObject.parse("" +
                "organisation: ORG-TR1-TEST\n" +
                "org-type: LIR\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
        when(update.isOverride()).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgChanged());
    }

    @Test
    public void sponsoringOrg_changed_override() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "sponsoring-org: ORG-SP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(objectDao.getByKey(ObjectType.ORGANISATION, CIString.ciString("ORG-TR1-TEST"))).thenReturn(RpslObject.parse("" +
                "organisation: ORG-TR1-TEST\n" +
                "org-type: LIR\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
        when(update.isOverride()).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgChanged());
    }

    @Test
    public void sponsoringOrg_changed_not_rsmaintainer() {
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-TR1-TEST\n" +
                "sponsoring-org: ORG-SP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(objectDao.getByKey(ObjectType.ORGANISATION, CIString.ciString("ORG-TR1-TEST"))).thenReturn(RpslObject.parse("" +
                "organisation: ORG-TR1-TEST\n" +
                "org-type: LIR\n" +
                "mnt-by: TEST-MNT\n" +
                "source: TEST"));
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")));
        when(update.isOverride()).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.sponsoringOrgNotLIR());
        verify(updateContext).addMessage(update, UpdateMessages.sponsoringOrgChanged());
    }
}
