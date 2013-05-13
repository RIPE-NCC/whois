package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OrgRefAuthenticationTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private AuthenticationModule credentialValidators;
    @Mock private RpslObjectDao rpslObjectDao;

    @InjectMocks private OrgRefAuthentication subject;

    @Test
    public void supports_update_with_new_org_references() {
        when(update.getNewValues(AttributeType.ORG)).thenReturn(ciSet("ORG2"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG1\norg: ORG1"));

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void no_difference_in_org_refs_is_not_supported() {
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("organisation: ORG1\norg: ORG1"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("organisation: ORG1\norg: ORG1"));

        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void authentication_succeeds() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\norg: ORG1"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\norg: ORG2"));

        final RpslObject organisation = RpslObject.parse("organisation: ORG1\nmnt-ref: REF-MNT");
        final List<RpslObject> organisations = Lists.newArrayList(organisation);
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyList())).thenReturn(organisations);

        final RpslObject maintainer = RpslObject.parse("mntner: REF-MNT");
        when(rpslObjectDao.getByKey(ObjectType.MNTNER, "REF-MNT")).thenReturn(maintainer);

        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(candidates);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(maintainer));
        verifyZeroInteractions(updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void no_mntnerref_found() {
        when(update.getType()).thenReturn(ObjectType.PERSON);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("person: Some One\nnetname: NETNAME\nnic-hdl: TEST-NIC\norg: ORG1"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("person: Some One\nnetname: NETNAME\nnic-hdl: TEST-NIC\norg: ORG2"));

        final RpslObject organisation = RpslObject.parse("organisation: ORG2");
        final List<RpslObject> organisations = Lists.newArrayList(organisation);
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyList())).thenReturn(organisations);

        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void mntnerref_does_not_exist() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\norg: ORG1"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\norg: ORG2"));

        final RpslObject organisation = RpslObject.parse("organisation: ORG1\nmnt-ref: REF-MNT");
        final List<RpslObject> organisations = Lists.newArrayList(organisation);
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyList())).thenReturn(organisations);

        final RpslObject maintainer = RpslObject.parse("mntner: REF-MNT");
        when(rpslObjectDao.getByKey(ObjectType.MNTNER, "REF-MNT")).thenThrow(EmptyResultDataAccessException.class);

        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);

        verify(updateContext).addMessage(eq(update), UpdateMessages.nonexistantMntRef("ORG1", "REF-MNT"));
    }
}
