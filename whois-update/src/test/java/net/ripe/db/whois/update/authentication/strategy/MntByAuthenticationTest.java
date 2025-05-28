package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MntByAuthenticationTest {
    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;

    @Mock Maintainers maintainers;
    @Mock AuthenticationModule credentialValidators;
    @Mock RpslObjectDao rpslObjectDao;
    @Mock Ipv4Tree ipv4Tree;
    @Mock SsoTranslator ssoTranslator;

    @InjectMocks MntByAuthentication subject;

    @Test
    public void supports_every_object_with_a_mntby_attribute() {
        when(update.getType()).thenReturn(ObjectType.POEM);
        assertThat(subject.supports(update), is(true));

        when(update.getType()).thenReturn(ObjectType.INETNUM);
        assertThat(subject.supports(update), is(true));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_succeeds() {
        final RpslObject org = RpslObject.parse("organisation: ORG1\nmnt-by: TEST-MNT");
        when(update.getReferenceObject()).thenReturn(org);
        when(update.getType()).thenReturn(ObjectType.ORGANISATION);
        final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT");
        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, org.getValuesForAttribute(AttributeType.MNT_BY))).thenReturn(candidates);

        when(credentialValidators.authenticate(update, updateContext, candidates, MntByAuthentication.class)).thenReturn(candidates);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(maintainer));

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_fails() {
        assertThrows(AuthenticationFailedException.class, () -> {
            final RpslObject person = RpslObject.parse("person: Some One\nnic-hdl: TEST-NIC\nmnt-by: TEST-MNT");
            when(update.getAction()).thenReturn(Action.MODIFY);
            when(update.getReferenceObject()).thenReturn(person);
            when(update.getType()).thenReturn(person.getType());
            final RpslObject maintainer = RpslObject.parse("mntner: TEST-MNT");
            final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);
            when(rpslObjectDao.getByKeys(ObjectType.MNTNER, person.getValuesForAttribute(AttributeType.MNT_BY))).thenReturn(candidates);
            when(credentialValidators.authenticate(update, updateContext, candidates, MntByAuthentication.class)).thenReturn(Lists.newArrayList());

            subject.authenticate(update, updateContext);

            verify(maintainers).isRsMaintainer(ciSet("RS-MNT"));
            verifyNoMoreInteractions(maintainers);
        });
    }

    @Test
    public void authenticate_create_self_reference_succeeds() {
        final RpslObject mntner = RpslObject.parse("mntner: TEST-MNT\nmnt-by: TEST-MNT");

        when(update.getType()).thenReturn(mntner.getType());
        when(update.getReferenceObject()).thenReturn(mntner);
        when(update.getAction()).thenReturn(Action.CREATE);

        final ArrayList<RpslObject> candidates = Lists.newArrayList(mntner);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, mntner.getValuesForAttribute(AttributeType.MNT_BY))).thenReturn(Lists.newArrayList());

        when(credentialValidators.authenticate(update, updateContext, candidates, MntByAuthentication.class)).thenReturn(candidates);

        when(ssoTranslator.translateFromCacheAuthToUuid(updateContext, mntner)).thenReturn(mntner);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(mntner));

        verifyNoMoreInteractions(updateContext);
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_modify_original_object_has_no_mntby_update() {
        when(update.getAction()).thenReturn(Action.MODIFY);

        final RpslObject original = RpslObject.parse("person: Mr Hubbard\nnic-hdl: TEST-NIC");

        when(update.getReferenceObject()).thenReturn(original);

        final RpslObject updated = RpslObject.parse("person: Mr Hubbard\nnic-hdl: TEST-NIC\nmnt-by: TEST-MNT");
        when(update.getUpdatedObject()).thenReturn(updated);

        when(update.getType()).thenReturn(ObjectType.PERSON);
        final List<RpslObject> candidates = Lists.newArrayList(RpslObject.parse("mntner: TEST-MNT"));
        when(rpslObjectDao.getByKeys(eq(ObjectType.MNTNER), anyCollection())).thenReturn(candidates);

        when(credentialValidators.authenticate(update, updateContext, candidates, MntByAuthentication.class)).thenReturn(candidates);

        final List<RpslObject> authenticate = subject.authenticate(update, updateContext);
        assertThat(authenticate, is(candidates));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_modify_original_object_has_no_mntby_delete() {
        final RpslObject original = RpslObject.parse("person: Mr Hubbard\nnic-hdl: TEST-NIC");
        final RpslObject updated = RpslObject.parse("person: Mr Hubbard\nnic-hdl: TEST-NIC\nmnt-by: TEST-MNT");

        when(update.getAction()).thenReturn(Action.DELETE);
        when(update.getReferenceObject()).thenReturn(original);

        verifyNoMoreInteractions(rpslObjectDao, credentialValidators);

        final List<RpslObject> authenticate = subject.authenticate(update, updateContext);
        assertThat(authenticate, hasSize(0));

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_mnt_by_fails_delete_with_rs_maintainer() {
        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:  193.0.0.0\n" +
                "mnt-by:   DEV1-MNT\n" +
                "mnt-by:   RS-MNT\n");

        when(update.getAction()).thenReturn(Action.DELETE);
        when(update.getReferenceObject()).thenReturn(inetnum);
        when(update.getUpdatedObject()).thenReturn(inetnum);
        when(update.getType()).thenReturn(inetnum.getType());

        final RpslObject maintainer = RpslObject.parse("mntner:   DEV1-MNT\n");
        final ArrayList<RpslObject> mntByCandidates = Lists.newArrayList(maintainer);
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, ciSet("DEV1-MNT", "RS-MNT"))).thenReturn(mntByCandidates);

        when(credentialValidators.authenticate(update, updateContext, mntByCandidates, MntByAuthentication.class)).thenReturn(Lists.newArrayList());

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(UpdateMessages.authenticationFailed(inetnum, AttributeType.MNT_BY, Lists.newArrayList(maintainer))));
        }

        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_mnt_by_fails_delete_parent_has_rs_fails_authentication() {
        lenient().when(maintainers.isRsMaintainer(ciSet("RS-MNT"))).thenReturn(true);

        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:  193.0.0.0\n" +
                "mnt-by:   DEV1-MNT\n");

        when(update.getAction()).thenReturn(Action.DELETE);
        when(update.getReferenceObject()).thenReturn(inetnum);
        when(update.getUpdatedObject()).thenReturn(inetnum);
        when(update.getType()).thenReturn(inetnum.getType());

        final ArrayList<RpslObject> mntByCandidates = Lists.newArrayList(RpslObject.parse("mntner:   DEV1-MNT\n"));
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, ciSet("DEV1-MNT"))).thenReturn(mntByCandidates);

        when(credentialValidators.authenticate(update, updateContext, mntByCandidates, MntByAuthentication.class)).thenReturn(Lists.newArrayList());

        final Ipv4Entry parent = new Ipv4Entry(Ipv4Resource.parse("193.0.0.0/24"), 1);
        when(ipv4Tree.findExactAndAllLessSpecific(Ipv4Resource.parse(inetnum.getKey()))).thenReturn(Lists.newArrayList(parent));

        final RpslObject ipObject = RpslObject.parse("" +
                "inetnum:   193.0.0.0/24\n" +
                "mnt-by:    RS-MNT");
        when(rpslObjectDao.getById(1)).thenReturn(ipObject);

        final ArrayList<RpslObject> parentCandidates = Lists.newArrayList(RpslObject.parse("mntner: RS-MNT"));
        lenient().when(rpslObjectDao.getByKeys(ObjectType.MNTNER, ciSet("RS-MNT"))).thenReturn(parentCandidates);

        try {
            subject.authenticate(update, updateContext);
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            assertThat(e.getAuthenticationMessages(), contains(
                    UpdateMessages.authenticationFailed(inetnum, AttributeType.MNT_BY, mntByCandidates),
                    UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_LOWER, Lists.newArrayList()),
                    UpdateMessages.authenticationFailed(ipObject, AttributeType.MNT_BY, parentCandidates)));
        }

        verify(maintainers).isRsMaintainer(ciSet());
        verify(maintainers).isRsMaintainer(ciSet("RS-MNT"));
        verifyNoMoreInteractions(maintainers);
    }

    @Test
    public void authenticate_mnt_by_fails_delete_parent_has_rs_success() {
        lenient().when(maintainers.isRsMaintainer(ciSet("RS-MNT"))).thenReturn(true);

        final RpslObject inetnum = RpslObject.parse("" +
                "inetnum:  193.0.0.0\n" +
                "mnt-by:   DEV1-MNT\n");

        when(update.getAction()).thenReturn(Action.DELETE);
        when(update.getReferenceObject()).thenReturn(inetnum);
        when(update.getUpdatedObject()).thenReturn(inetnum);
        when(update.getType()).thenReturn(inetnum.getType());

        final ArrayList<RpslObject> mntByCandidates = Lists.newArrayList(RpslObject.parse("mntner:   DEV1-MNT\n"));
        when(rpslObjectDao.getByKeys(ObjectType.MNTNER, ciSet("DEV1-MNT"))).thenReturn(mntByCandidates);

        when(credentialValidators.authenticate(update, updateContext, mntByCandidates, MntByAuthentication.class)).thenReturn(Lists.newArrayList());

        final Ipv4Entry parent = new Ipv4Entry(Ipv4Resource.parse("193.0.0.0/24"), 1);
        when(ipv4Tree.findExactAndAllLessSpecific(Ipv4Resource.parse(inetnum.getKey()))).thenReturn(Lists.newArrayList(parent));

        final RpslObject ipObject = RpslObject.parse("" +
                "inetnum:   193.0.0.0/24\n" +
                "mnt-by:    RS-MNT");
        when(rpslObjectDao.getById(1)).thenReturn(ipObject);

        final List<RpslObject> parentCandidates = Lists.newArrayList(RpslObject.parse("mntner: RS-MNT"));
        lenient().when(rpslObjectDao.getByKeys(ObjectType.MNTNER, ciSet("RS-MNT"))).thenReturn(parentCandidates);
        when(credentialValidators.authenticate(
                eq(update),
                eq(updateContext),
                argThat(argument -> argument.containsAll(parentCandidates)),
                eq(MntByAuthentication.class)))
            .thenReturn(parentCandidates);

        final List<RpslObject> authenticated = subject.authenticate(update, updateContext);
        assertThat(authenticated, is(parentCandidates));

        verify(maintainers).isRsMaintainer(ciSet("RS-MNT"));
        verify(maintainers).isRsMaintainer(ciSet());
        verifyNoMoreInteractions(maintainers);
    }
}
