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
import java.util.HashSet;

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

    public static final RpslObject ORIGINAL_ORG = RpslObject.parse(10,
            "organisation: ORG-TEST1\n" +
            "org-name: Test Organisation\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_ORG_SAME_NAME = RpslObject.parse(20,
            "organisation: ORG-TEST1\n" +
            "org-name: Test Organisation\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_ORG_NEW_NAME = RpslObject.parse(30,
            "organisation: ORG-TEST1\n" +
            "org-name: Updated Organisation\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject REFERRER_MNT_BY_USER = RpslObject.parse(40,
            "aut-num: AS3434\n" +
            "mnt-by: TEST-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");
    public static final RpslObject REFERRER_MNT_BY_RS = RpslObject.parse(50,
            "aut-num: AS3434\n" +
            "mnt-by: RIPE-NCC-HM-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");
    public static final RpslObject REFERRER_MNT_BY_LEGACY = RpslObject.parse(60,
            "aut-num: AS3434\n" +
            "mnt-by: RIPE-NCC-LEGACY-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");

    @Before
    public void setup() {
        when(maintainers.getRsMaintainers()).thenReturn(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT"), CIString.ciString("RIPE-NCC-END-MNT"), CIString.ciString("RIPE-NCC-LEGACY-MNT")));
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
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_SAME_NAME);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_not_referenced_at_all() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);

        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        when(updateDao.getReferences(ORIGINAL_ORG)).thenReturn(Collections.EMPTY_SET);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_not_referenced_by_resource() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        when(updateDao.getReferences(ORIGINAL_ORG)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.PERSON, "TEST-NIC")));

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_without_RSmntner__no_RSmntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_USER);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__no_RSmntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_RS);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.cantChangeOrgName());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__no_LEGACY_mntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);

        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_LEGACY);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.cantChangeOrgName());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RS_and_LEGACY_mntner__no_LEGACY_mntner_auth() {
        presetOverrideAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_RS, REFERRER_MNT_BY_LEGACY);

        subject.validate(update, updateContext);

        // HM and LEGACY maintainers belong to the same group of RS super-mntners
        // Any mntner from that group could be used to update such objects
        // confirmed by David 2014-10-06
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.cantChangeOrgName());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__RSmaintainer_auth() {
        presetOverrideAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_RS);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__LEGACY_maintainer_auth() {
        presetRsAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_LEGACY);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__auth_by_override() {
        presetOverrideAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_RS);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__auth_by_override() {
        presetOverrideAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_LEGACY);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RS_and_LEGACY_mntner__auth_by_override() {
        presetOverrideAuthentication();

        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);

        presetReferrers(REFERRER_MNT_BY_RS, REFERRER_MNT_BY_LEGACY);

        subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<Message>anyObject());
        verify(updateContext, never()).addMessage(Matchers.<Update>anyObject(), Matchers.<RpslAttribute>anyObject(), Matchers.<Message>anyObject());
    }

    private void presetRsAuthentication() {
        when(subjectObject.hasPrincipal(Matchers.eq(Principal.RS_MAINTAINER))).thenReturn(true);
    }

    private void presetOverrideAuthentication() {
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
    }

    private void presetReferrers(RpslObject... referrerObjects) {
        final HashSet<RpslObjectInfo> rpslObjectInfos = new HashSet<>(referrerObjects.length);

        for (RpslObject referrerObject : referrerObjects) {
            rpslObjectInfos.add(
                    new RpslObjectInfo(referrerObject.getObjectId(), referrerObject.getType(), referrerObject.getKey()));
            when(objectDao.getById(referrerObject.getObjectId())).thenReturn(referrerObject);
        }

        when(updateDao.getReferences(ORIGINAL_ORG)).thenReturn(rpslObjectInfos);
    }
}
