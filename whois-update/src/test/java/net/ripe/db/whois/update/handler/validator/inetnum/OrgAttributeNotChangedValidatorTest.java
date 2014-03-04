package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrgAttributeNotChangedValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock private Subject subjectObject;
    @Mock private Maintainers maintainers;
    @InjectMocks private OrgAttributeNotChangedValidator subject;

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(CIString.ciSet("RIPE-NCC-HM-MNT", "RIPE-NCC-END-MNT"));
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM, ObjectType.AUT_NUM));
    }

    @Test
    public void validate_org_attribute_not_changed() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }

    @Test
    public void validate_has_no_org_attribute() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }

    @Test
    public void validate_resource_not_maintained_by_ripe() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-PQ-TEST\n" +
                "mnt-by: OTHER-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);
        when(update.isOverride()).thenReturn(Boolean.FALSE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }

    @Test
    public void validate_resource_maintained_by_ripe_auth_by_other_mnt() {
        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-PQ-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);
        when(update.isOverride()).thenReturn(Boolean.FALSE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }

    @Test
    public void validate_resource_maintained_by_ripe() {
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.TRUE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-WG-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);

        when(update.isOverride()).thenReturn(Boolean.FALSE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_override() {
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: RIPE-NCC-END-MNT");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-WG-TEST\n" +
                "mnt-by: RIPE-NCC-END-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);

        when(update.isOverride()).thenReturn(Boolean.TRUE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(update, UpdateMessages.cantChangeOrgAttribute());
    }
}
