package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.ReferencesDao;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.domain.CIString;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrgNameAndCountryAttrValidatorTest {
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock private Subject subjectObject;
    @Mock private RpslObjectDao objectDao;
    @Mock private ReferencesDao referencesDao;
    @Mock private Maintainers maintainers;
    @InjectMocks private OrgNameAndCountryAttrValidator subject;

    public static final RpslObject ORIGINAL_ORG = RpslObject.parse(10, "" +
            "organisation: ORG-TEST1\n" +
            "org-name: Test Organisation\n" +
            "org-type: OTHER\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_ORG_SAME_NAME = RpslObject.parse(20, "" +
            "organisation: ORG-TEST1\n" +
            "org-name: Test Organisation\n" +
            "org-type: OTHER\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_ORG_CASE_CHANGE = RpslObject.parse(20, "" +
            "organisation: ORG-TEST1\n" +
            "org-name: TEST Organisation\n" +
            "org-type: OTHER\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_ORG_NEW_NAME = RpslObject.parse(30, "" +
            "organisation: ORG-TEST1\n" +
            "org-name: Updated Organisation\n" +
            "org-type: OTHER\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject REFERRER_MNT_BY_USER = RpslObject.parse(40, "" +
            "aut-num: AS3434\n" +
            "mnt-by: TEST-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");
    public static final RpslObject REFERRER_MNT_BY_RS = RpslObject.parse(50, "" +
            "aut-num: AS3434\n" +
            "mnt-by: RIPE-NCC-HM-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");
    public static final RpslObject REFERRER_MNT_BY_LEGACY = RpslObject.parse(60, "" +
            "aut-num: AS3434\n" +
            "mnt-by: RIPE-NCC-LEGACY-MNT\n" +
            "org: ORG-TEST1\n" +
            "source: TEST");
    public static final RpslObject ORIGINAL_LIR = RpslObject.parse(70, "" +
            "organisation: ORG-TEST2\n" +
            "org-name: Test Organisation\n" +
            "org-type: LIR\n" +
            "mnt-by: TEST-MNT");
    public static final RpslObject UPDATED_LIR = RpslObject.parse(70, "" +
            "organisation: ORG-TEST2\n" +
            "org-name: Test Organisation\n" +
            "org-type: LIR\n" +
            "mnt-by: TEST-MNT");

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(update)).thenReturn(subjectObject);
    }

    @AfterEach
    public void reset() {
        Mockito.reset(subjectObject);
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

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_not_referenced_at_all() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        when(referencesDao.getReferences(ORIGINAL_ORG)).thenReturn(Collections.EMPTY_SET);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_for_lir() {
        // See: LirRipeMaintainedAttributesValidator
        presetOverrideAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_LIR);
        when(update.getUpdatedObject()).thenReturn(UPDATED_LIR);
        presetReferrers(REFERRER_MNT_BY_RS, REFERRER_MNT_BY_LEGACY);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_not_referenced_by_resource() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        when(referencesDao.getReferences(ORIGINAL_ORG)).thenReturn(Sets.newHashSet(new RpslObjectInfo(5, ObjectType.PERSON, "TEST-NIC")));

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_without_RSmntner__no_RSmntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_USER);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(maintainers).isRsMaintainer(ciSet("TEST-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__no_RSmntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        when(maintainers.isRsMaintainer(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")))).thenReturn(true);
        presetReferrers(REFERRER_MNT_BY_RS);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.canOnlyBeChangedByRipeNCC(UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__no_LEGACY_mntner_auth() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        when(maintainers.isRsMaintainer(Sets.newHashSet(CIString.ciString("RIPE-NCC-LEGACY-MNT")))).thenReturn(true);
        presetReferrers(REFERRER_MNT_BY_LEGACY);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.canOnlyBeChangedByRipeNCC(UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-LEGACY-MNT"));
        verifyNoMoreInteractions(maintainers);
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
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(update, UPDATED_ORG_NEW_NAME.findAttribute(AttributeType.ORG_NAME), UpdateMessages.canOnlyBeChangedByRipeNCC(AttributeType.ORG_NAME));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__RSmaintainer_auth() {
        presetOverrideAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_RS);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__LEGACY_maintainer_auth() {
        presetRsAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_LEGACY);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner__auth_by_override() {
        presetOverrideAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_RS);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_LEGACY_mntner__auth_by_override() {
        presetOverrideAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_LEGACY);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RS_and_LEGACY_mntner__auth_by_override() {
        presetOverrideAuthentication();
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_NEW_NAME);
        presetReferrers(REFERRER_MNT_BY_RS, REFERRER_MNT_BY_LEGACY);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void orgname_changed_referenced_by_resource_with_RSmntner_change_is_case_sensitive() {
        when(update.getReferenceObject()).thenReturn(ORIGINAL_ORG);
        when(update.getUpdatedObject()).thenReturn(UPDATED_ORG_CASE_CHANGE);
        when(maintainers.isRsMaintainer(Sets.newHashSet(CIString.ciString("RIPE-NCC-HM-MNT")))).thenReturn(true);
        presetReferrers(REFERRER_MNT_BY_RS);

       subject.validate(update, updateContext);

        verify(updateContext, never()).addMessage(ArgumentMatchers.any(), ArgumentMatchers.any());
        verify(updateContext).addMessage(update, ORIGINAL_ORG.findAttribute(AttributeType.ORG_NAME), UpdateMessages.canOnlyBeChangedByRipeNCC(ORIGINAL_ORG.findAttribute(AttributeType.ORG_NAME)));
        verify(maintainers).isRsMaintainer(ciSet("RIPE-NCC-HM-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    // helper methods

    private void presetRsAuthentication() {
        lenient().when(subjectObject.hasPrincipal(ArgumentMatchers.eq(Principal.RS_MAINTAINER))).thenReturn(true);
    }

    private void presetOverrideAuthentication() {
        when(subjectObject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(true);
    }

    private void presetReferrers(RpslObject... referrerObjects) {
        final HashSet<RpslObjectInfo> rpslObjectInfos = new HashSet<>(referrerObjects.length);

        for (RpslObject referrerObject : referrerObjects) {
            rpslObjectInfos.add(
                    new RpslObjectInfo(referrerObject.getObjectId(), referrerObject.getType(), referrerObject.getKey()));
            lenient().when(objectDao.getById(referrerObject.getObjectId())).thenReturn(referrerObject);
        }

        lenient().when(referencesDao.getReferences(ORIGINAL_ORG)).thenReturn(rpslObjectInfos);
    }
}
