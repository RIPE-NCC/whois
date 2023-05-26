package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleRefAuthenticationTest {

    @Mock
    private PreparedUpdate update;

    @Mock private UpdateContext updateContext;

    @Mock private AuthenticationModule credentialValidators;

    @Mock private RpslObjectDao rpslObjectDao;

    @InjectMocks
    private RoleRefAuthentication subject;

    @Test
    public void supports_update_with_new_role_references() {
        when(update.getNewValues(AttributeType.ADMIN_C)).thenReturn(ciSet("PPT1"));

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void no_difference_in_role_refs_is_not_supported() {
        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void authentication_succeeds() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        final RpslObject role = RpslObject.parse("role: tester\nnic-hdl: PPT1-TEST\nmnt-ref: REF-MNT");
        final List<RpslObject> roles = Lists.newArrayList(role);
        when(rpslObjectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn(roles);

        final RpslObject maintainer = RpslObject.parse("mntner: REF-MNT");
        when(rpslObjectDao.getByKey(ObjectType.MNTNER, "REF-MNT")).thenReturn(maintainer);

        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList(), eq(RoleRefAuthentication.class))).thenReturn(candidates);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(maintainer));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void no_mntnerref_found_not_error_thrown() {
        when(update.getType()).thenReturn(ObjectType.ORGANISATION);

        final List<RpslObject> roles = Lists.newArrayList(RpslObject.parse("role: tester\nnic-hdl: " +
                "PPT1-TEST"));
        when(rpslObjectDao.getByKeys(eq(ObjectType.ROLE), anyCollection())).thenReturn((roles));

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result, is(emptyList()));
    }
}
