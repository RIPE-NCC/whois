package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MntIrtAuthenticationTest {

    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate update;
    @Mock private AuthenticationModule credentialValidators;
    @Mock private RpslObjectDao rpslObjectDao;

    @InjectMocks private MntIrtAuthentication subject;

    @Test
    public void operates_on_updates_with_new_mntirts() {
        when(update.getNewValues(AttributeType.MNT_IRT)).thenReturn(ciSet("IRT-MNT"));
        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void does_not_support_updates_with_same_mntirts() {
        when(update.getNewValues(AttributeType.MNT_IRT)).thenReturn(Sets.<CIString>newHashSet());
        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void authentication_succeeds() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);

        when(update.getNewValues(AttributeType.MNT_IRT)).thenReturn(ciSet("IRT-MNT"));
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24"));
        when(update.getUpdatedObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nmnt-irt: IRT-MNT"));

        final RpslObject irt = RpslObject.parse("irt: IRT-MNT");

        final ArrayList<RpslObject> irts = Lists.newArrayList(irt);
        when(rpslObjectDao.getByKeys(ObjectType.IRT, ciSet("IRT-MNT"))).thenReturn(irts);
        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyCollection())).thenReturn(irts);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(irt));
        verifyZeroInteractions(updateContext);
    }

    @Test(expected = AuthenticationFailedException.class)
    public void authentication_fails() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        when(update.getReferenceObject()).thenReturn(RpslObject.parse("inetnum: 192.0/24\nmnt-irt: IRT-MNT"));
        final RpslObject irt = RpslObject.parse("irt: IRT-MNT");

        final ArrayList<RpslObject> irts = Lists.newArrayList(irt);
        when(rpslObjectDao.getByKeys(ObjectType.IRT, ciSet("IRT-MNT"))).thenReturn(irts);
        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyCollection())).thenReturn(Lists.<RpslObject>newArrayList());

        subject.authenticate(update, updateContext);
    }
}
