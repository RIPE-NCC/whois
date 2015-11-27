package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectUpdateDao;
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

import java.util.Collections;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbuseValidatorTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock JdbcRpslObjectDao objectDao;
    @Mock JdbcRpslObjectUpdateDao updateDao;
    @Mock Maintainers maintainers;

    @InjectMocks AbuseValidator subject;

    @Test
    public void getActions() {
        assertThat(subject.getActions(), containsInAnyOrder(Action.CREATE, Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), containsInAnyOrder(ObjectType.ORGANISATION));
    }

    @Test
    public void hasNoAbuseC() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\n" +
                                                                    "org-type: OTHER"));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void referencesRoleWithoutAbuseMailbox() {
        final RpslObject organisation = RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER");
        when(update.getUpdatedObject()).thenReturn(organisation);

        final RpslObject role = RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC");
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(role));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseMailboxRequired(role.getKey()));
    }

    @Test
    public void referencesRoleWithAbuseMailbox() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net")));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void referencesPersonInsteadOfRole() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Collections.EMPTY_LIST);
        when(objectDao.getByKeys(eq(ObjectType.PERSON), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("person: Some Person\nnic-hdl: AB-NIC")));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseCPersonReference());
        verify(updateContext, never()).addMessage(update, UpdateMessages.abuseMailboxRequired("nic-hdl: AB-NIC"));
    }

    @Test
    public void referenceNotFound() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Collections.EMPTY_LIST);
        when(objectDao.getByKeys(eq(ObjectType.PERSON), anyCollection())).thenReturn(Collections.EMPTY_LIST);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void removeAbuseCContact_LIR() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: LIR"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\norg-type: LIR"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseContactNotRemovable());
    }

    @Test
    public void allow_removeAbuseCContact_when_non_LIR_no_referencing_objects() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net")));
        when(updateDao.getReferences(update.getReferenceObject())).thenReturn(Collections.EMPTY_SET);

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }


    @Test
    public void allow_removeAbuseC_when_referencing_object_is_not_resource() {
        RpslObject referencingPerson = RpslObject.parse("person: A Person\naddress: Address 1\nphone: +31 20 535 4444\nnic-hdl: DUMY-RIPE\norg: ORG-1\nmnt-by: A_NON_RS_MAINTAINER\nchanged: ripe-dbm@ripe.net 20090724\nsource: RIPE");
        RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.PERSON, "a");

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net")));
        when(updateDao.getReferences(update.getReferenceObject())).thenReturn(Sets.newHashSet(info));
        when(objectDao.getById(1)).thenReturn(referencingPerson);
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-DBM-MNT")));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);
    }

    @Test
    public void allow_removeAbuseC_when_referencing_resources_not_rsMaintained() {
        RpslObject resource = RpslObject.parse("aut-num: AS6\norg: ORG-1\nmnt-by: A_NON_RS_MAINTAINER");
        RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.AUT_NUM, "a");

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net")));
        when(updateDao.getReferences(update.getReferenceObject())).thenReturn(Sets.newHashSet(info));
        when(objectDao.getById(1)).thenReturn(resource);
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("AN_RS_MAINTAINER")));

        subject.validate(update, updateContext);

        verifyZeroInteractions(updateContext);

    }

    @Test
    public void not_allow_removeAbuseC_when_a_referencing_resource_is_rsmaintained() {
        RpslObject resource = RpslObject.parse("aut-num: AS6\norg: ORG-1\nmnt-by: AN_RS_MAINTAINER");

        RpslObjectInfo info = new RpslObjectInfo(1, ObjectType.AUT_NUM, "a");

        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG-1\nabuse-c: AB-NIC\norg-type: OTHER"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG-1\norg-type: OTHER"));
        when(objectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(Lists.newArrayList(RpslObject.parse("role: Role Test\nnic-hdl: AB-NIC\nabuse-mailbox: abuse@test.net")));
        when(updateDao.getReferences(update.getReferenceObject())).thenReturn(Sets.newHashSet(info));
        when(objectDao.getById(1)).thenReturn(resource);
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("AN_RS_MAINTAINER")));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.abuseContactNotRemovable());
    }

}
