package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OrgNameNotChangedValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock private Subject subjectObject;
    @Mock private RpslObjectUpdateDao updateDao;
    @Mock private RpslObjectDao objectDao;
    @Mock private Maintainers maintainers;
    @InjectMocks private OrgNameNotChangedValidator subject;

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT"), CIString.ciString("RIPE-NCC-END-MNT")));
        when(updateContext.getSubject(update)).thenReturn(subjectObject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions(), contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes(), contains(ObjectType.ORGANISATION));
    }

    @Test
    public void orgname_not_changed() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");

        when(update.getReferenceObject()).thenReturn(object);
        when(update.getUpdatedObject()).thenReturn(object);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_not_referenced_at_all() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Collections.EMPTY_SET);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_not_referenced_by_resource() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.PERSON, "TEST-NIC")));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_without_RSmntner__no_RSmntner_auth() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.AUT_NUM, "AS123")));
        when(objectDao.getById(5)).thenReturn(RpslObject.parse("" +
                "aut-num: AS3434\n" +
                "mnt-by: TEST-MNT\n" +
                "org: ORG-TEST1\n" +
                "source: TEST"));

        when(update.isOverride()).thenReturn(Boolean.FALSE);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__no_RSmntner_auth() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: RIPE-NCC-HM-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.AUT_NUM, "AS123")));
        when(objectDao.getById(5)).thenReturn(RpslObject.parse("" +
                "aut-num: AS3434\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "org: ORG-TEST1\n" +
                "source: TEST"));

        when(update.isOverride()).thenReturn(Boolean.FALSE);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.FALSE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext).addMessage(update, updatedObject.findAttribute(AttributeType.ORG_NAME), UpdateMessages.cantChangeOrgName());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__RSmaintainer_auth() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.AUT_NUM, "AS123")));
        when(objectDao.getById(5)).thenReturn(RpslObject.parse("" +
                "aut-num: AS3434\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "org: ORG-TEST1\n" +
                "source: TEST"));

        when(update.isOverride()).thenReturn(Boolean.FALSE);
        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(Boolean.TRUE);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__auth_by_override() {
        final RpslObject object = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Test Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(object);

        final RpslObject updatedObject = RpslObject.parse("" +
                "organisation: ORG-TEST1\n" +
                "org-name: Updated Organisation" +
                "mnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updatedObject);

        when(updateDao.getReferences(object)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.AUT_NUM, "AS123")));
        when(objectDao.getById(5)).thenReturn(RpslObject.parse("" +
                "aut-num: AS3434\n" +
                "mnt-by: RIPE-NCC-END-MNT\n" +
                "org: ORG-TEST1\n" +
                "source: TEST"));

        when(subjectObject.hasPrincipal(Principal.RS_MAINTAINER)).thenReturn(false);
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }
}
