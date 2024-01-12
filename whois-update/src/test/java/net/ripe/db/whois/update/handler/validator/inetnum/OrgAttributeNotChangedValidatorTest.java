package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OrgAttributeNotChangedValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock private Subject subjectObject;

    @Mock private Maintainers maintainers;
    @InjectMocks private OrgAttributeNotChangedValidator subject;

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(update)).thenReturn(subjectObject);
        lenient().when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"))).thenReturn(true);
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
    public void validate_org_attribute_not_changed_for_aut_num() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_org_attribute_not_changed_for_inetnum() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_org_attribute_not_changed_for_inet6num() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:     2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_has_no_org_attribute_for_aut_num() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_has_no_org_attribute_for_inetnum() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_has_no_org_attribute_for_inet6num() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "inet6num:     2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(rpslObject);
        when(update.getUpdatedObject()).thenReturn(rpslObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_for_aut_num() {
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
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_for_inetnum() {
        final RpslObject original = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OTHER-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_for_inet6num() {
        final RpslObject original = RpslObject.parse("" +
                "inet6num:     2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OTHER-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inet6num:     2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OTHER-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "source:       TEST");
        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("OTHER-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_modify_resource_maintained_by_ripe_auth_by_other_mnt_for_aut_num() {
        when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"))).thenReturn(true);

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
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, updated.findAttribute(AttributeType.ORG), UpdateMessages.cantChangeOrgAttribute(updated.findAttribute(AttributeType.ORG)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_modify_resource_maintained_by_ripe_auth_by_other_mnt_for_inetnum() {
        final RpslObject original = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, updated.findAttribute(AttributeType.ORG),UpdateMessages.cantChangeOrgAttribute(updated.findAttribute(AttributeType.ORG)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_modify_resource_maintained_by_ripe_auth_by_other_mnt_for_inet6num() {
        final RpslObject original = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, updated.findAttribute(AttributeType.ORG), UpdateMessages.cantChangeOrgAttribute(updated.findAttribute(AttributeType.ORG)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_delete_resource_maintained_by_ripe_auth_by_other_mnt_for_aut_num() {
        when(maintainers.isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"))).thenReturn(true);

        final RpslObject original = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "org: ORG-RT-TEST\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "aut-num: AS123\n" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.cantRemoveOrgAttribute());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_delete_resource_maintained_by_ripe_auth_by_other_mnt_for_inetnum() {
        final RpslObject original = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.cantRemoveOrgAttribute());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_delete_resource_maintained_by_ripe_auth_by_other_mnt_for_inet6num() {
        final RpslObject original = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(updated);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.cantRemoveOrgAttribute());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT", "TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_maintained_by_ripe_for_aut_num() {
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

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_maintained_by_ripe_for_inetnum() {
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.TRUE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_maintained_by_ripe_for_inet6num() {
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.TRUE);
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

        final RpslObject original = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");


        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");


       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_override_for_aut_num() {
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


       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-END-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_override_for_inetnum() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

        final RpslObject original = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      192.168.0.0 - 192.169.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");
        when(update.getUpdatedObject()).thenReturn(updated);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void validate_resource_not_maintained_by_ripe_override_for_inet6num() {
        when(updateContext.getSubject(update)).thenReturn(subjectObject);

        final RpslObject original = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("" +
                "inetnum:      2001:600:1:1:1:1:1:1/64\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR2-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       TEST-MNT\n" +
                "source:       TEST");

        when(update.getUpdatedObject()).thenReturn(updated);


       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }
}
