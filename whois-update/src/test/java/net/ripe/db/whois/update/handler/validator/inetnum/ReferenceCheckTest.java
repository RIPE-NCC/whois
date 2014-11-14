package net.ripe.db.whois.update.handler.validator.inetnum;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceCheckTest {

    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private RpslObjectUpdateDao rpslObjectUpdateDao;
    @Mock private RpslObjectDao rpslObjectDao;
    @Mock private RpslObjectInfo rpslObjectInfo;
    @Mock Subject updateSubject;

    @InjectMocks private ReferenceCheck subject;

    @Before
    public void setup() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(updateSubject);
    }

    @Test
    public void modify_org_reference_missing() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.orgAttributeMissing());
    }

    @Test
    public void modify_org_reference_missing_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_org_reference_not_found_in_db() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(null);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.referenceNotFound("ORG1"));
    }

    @Test
    public void modify_org_reference_not_found_in_db_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(null);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_wrong_orgtype_on_found_org() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: OTHER"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.wrongOrgType(InetnumStatus.ALLOCATED_PA.getAllowedOrgTypes()));
    }

    @Test
    public void modify_wrong_orgtype_on_found_org_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: OTHER"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void modify_right_orgtype_on_found_org() {
        when(update.getAction()).thenReturn(Action.MODIFY);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: LIR"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void create_org_reference_missing() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.orgAttributeMissing());
    }

    @Test
    public void create_org_reference_missing_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void create_org_reference_not_found_in_db() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(null);

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.referenceNotFound("ORG1"));
    }

    @Test
    public void create_org_reference_not_found_in_db_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(null);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void create_wrong_orgtype_on_found_org() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: OTHER"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.wrongOrgType(InetnumStatus.ALLOCATED_PA.getAllowedOrgTypes()));
    }

    @Test
    public void create_wrong_orgtype_on_found_org_direct_assignment() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PI\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: DIRECT_ASSIGNMENT"));

        subject.validate(update, updateContext);

        verify(updateContext).addMessage(update, UpdateMessages.wrongOrgType(InetnumStatus.ALLOCATED_PI.getAllowedOrgTypes()));
    }

    @Test
    public void create_wrong_orgtype_on_found_org_override() {
        when(updateSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: OTHER"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void create_right_orgtype_on_found_org() {
        when(update.getAction()).thenReturn(Action.CREATE);
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nstatus: ALLOCATED PA\norg: ORG1"));
        when(rpslObjectUpdateDao.getAttributeReference(AttributeType.ORG, ciString("ORG1"))).thenReturn(rpslObjectInfo);
        when(rpslObjectInfo.getKey()).thenReturn("ORG1");
        when(rpslObjectDao.getByKey(ObjectType.ORGANISATION, "ORG1")).thenReturn(RpslObject.parse("organisation: ORG1\norg-type: LIR"));

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(any(UpdateContainer.class));
        verifyNoMoreInteractions(updateContext);
    }
}
