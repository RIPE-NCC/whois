package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Collections;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbuseValidatorTest {

    private static final RpslObject LIR_ORG_WITH_ABUSE_C = RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: LIR\nsource: TEST");
    private static final RpslObject LIR_ORG_WITHOUT_ABUSE_C = RpslObject.parse("organisation: ORG-1\norg-type: LIR\nsource: TEST");
    private static final RpslObject OTHER_ORG_WITHOUT_ABUSE_C = RpslObject.parse("organisation: ORG-1\norg-type: OTHER\nsource: TEST");
    private static final RpslObject OTHER_ORG_WITH_ABUSE_C = RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER\nsource: TEST");

    private static final RpslObject RESOURCE_RS_MAINTAINED = RpslObject.parse("aut-num: AS6\norg: ORG-1\nmnt-by: AN_RS_MAINTAINER\nsource: TEST");
    private static final RpslObject RESOURCE_NOT_RS_MAINTAINED = RpslObject.parse("aut-num: AS6\norg: ORG-1\nmnt-by: A_NON_RS_MAINTAINER\nsource: TEST");

    private static final RpslObject ROLE_WITHOUT_ABUSE_MAILBOX = RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nsource: TEST");
    private static final RpslObject ROLE_WITH_ABUSE_MAILBOX = RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net\nsource: TEST");
    private static final RpslObject PERSON_WITHOUT_ABUSE_MAILBOX = RpslObject.parse("person: Some Person\nnic-hdl: AB-NIC");

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock JdbcRpslObjectDao objectDao;
    @Mock ReferencesDao referencesDao;
    @Mock Maintainers maintainers;

    @InjectMocks AbuseValidator subject;

    @BeforeEach
    public void setup() {
        when(update.getAction()).thenReturn(Action.MODIFY);
    }


    @Test
    public void has_no_abusec() {
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITHOUT_ABUSE_C);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void references_role_without_abuse_mailbox() {
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(objectDao.getByKey(eq(ObjectType.ROLE), eq(CIString.ciString("AB-NIC")))).thenReturn(ROLE_WITHOUT_ABUSE_MAILBOX);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseMailboxRequired("AB-NIC", update.getUpdatedObject().getType()));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void references_role_with_abuse_mailbox() {
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(objectDao.getByKey(eq(ObjectType.ROLE), eq(CIString.ciString("AB-NIC")))).thenReturn(ROLE_WITH_ABUSE_MAILBOX);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void references_person_instead_of_role() {
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(objectDao.getByKey(eq(ObjectType.ROLE), eq(CIString.ciString("AB-NIC")))).thenThrow(new EmptyResultDataAccessException(1));
        when(objectDao.getByKey(eq(ObjectType.PERSON), eq(CIString.ciString("AB-NIC")))).thenReturn(PERSON_WITHOUT_ABUSE_MAILBOX);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseCPersonReference());
        verify(updateContext, never()).addMessage(update, UpdateMessages.abuseMailboxRequired("nic-hdl: AB-NIC", update.getUpdatedObject().getType()));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void reference_not_found() {
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(objectDao.getByKey(eq(ObjectType.ROLE), eq(CIString.ciString("AB-NIC")))).thenThrow(new EmptyResultDataAccessException(1));
        when(objectDao.getByKey(eq(ObjectType.PERSON), eq(CIString.ciString("AB-NIC")))).thenThrow(new EmptyResultDataAccessException(1));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void remove_abuse_contact_LIR_org() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG_WITH_ABUSE_C);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_WITHOUT_ABUSE_C);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseContactNotRemovable());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void allow_remove_abuse_contact_when_non_LIR_no_referencing_objects() {
        when(update.getReferenceObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITHOUT_ABUSE_C);
        when(referencesDao.getReferences(update.getUpdatedObject())).thenReturn(Collections.EMPTY_SET);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }


    @Test
    public void allow_remove_abusec_when_referencing_object_is_not_resource() {
        final RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.PERSON, "AS6");
        when(update.getReferenceObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITHOUT_ABUSE_C);
        when(referencesDao.getReferences(update.getUpdatedObject())).thenReturn(Sets.newHashSet(info));

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void allow_remove_abuse_contact_when_referencing_resources_is_not_rs_maintained() {
        final RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.AUT_NUM, "AS6");
        when(update.getReferenceObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITHOUT_ABUSE_C);
        when(referencesDao.getReferences(update.getUpdatedObject())).thenReturn(Sets.newHashSet(info));
        when(objectDao.getById(info.getObjectId())).thenReturn(RESOURCE_NOT_RS_MAINTAINED);

       subject.validate(update, updateContext);

        verifyNoMoreInteractions(updateContext);
        verify(maintainers).isRsMaintainer(ciSet("A_NON_RS_MAINTAINER"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void do_not_allow_remove_abuse_contact_when_a_referencing_resource_is_rs_maintained() {
        when(maintainers.isRsMaintainer(Sets.newHashSet(CIString.ciString("AN_RS_MAINTAINER")))).thenReturn(true);

        final RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.AUT_NUM, "AS6");
        when(update.getReferenceObject()).thenReturn(OTHER_ORG_WITH_ABUSE_C);
        when(update.getUpdatedObject()).thenReturn(OTHER_ORG_WITHOUT_ABUSE_C);
        when(referencesDao.getReferences(update.getUpdatedObject())).thenReturn(Sets.newHashSet(info));
        when(objectDao.getById(info.getObjectId())).thenReturn(RESOURCE_RS_MAINTAINED);

       subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseContactNotRemovable());
        verify(maintainers).isRsMaintainer(ciSet("AN_RS_MAINTAINER"));
        verifyNoMoreInteractions(maintainers);
    }

}
