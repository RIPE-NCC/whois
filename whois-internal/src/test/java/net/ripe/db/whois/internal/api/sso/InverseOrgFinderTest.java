package net.ripe.db.whois.internal.api.sso;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InverseOrgFinderTest {
    @Mock RpslObjectDao objectDao;
    @Mock RpslObjectUpdateDao updateDao;

    InverseOrgFinder subject;

    @Before
    public void setup() {
        subject = new InverseOrgFinder();
        subject.setObjectDao(objectDao);
        subject.setUpdateDao(updateDao);
    }

    @Test
    public void no_such_auth() {
        when(objectDao.findByAttribute(AttributeType.AUTH, "SSO test@ripe.net")).thenReturn(Collections.EMPTY_LIST);

        final Set<RpslObject> organisationsForUser = subject.findOrganisationsForAuth("SSO test@ripe.net");
        assertThat(organisationsForUser, empty());
    }

    @Test
    public void no_referenced_organisation() {
        when(objectDao.findByAttribute(AttributeType.AUTH, "SSO test@ripe.net")).thenReturn(Collections.singletonList(new RpslObjectInfo(2, ObjectType.MNTNER, "SSO-MNT")));
        when(updateDao.getAttributeReference(AttributeType.MNT_REF, CIString.ciString("SSO-MNT"))).thenReturn(null);

        final Set<RpslObject> organisationsForUser = subject.findOrganisationsForAuth("SSO test@ripe.net");
        assertThat(organisationsForUser, empty());
    }

    @Test
    public void organisation_found() {
        when(objectDao.findByAttribute(AttributeType.AUTH, "SSO test@ripe.net")).thenReturn(Collections.singletonList(new RpslObjectInfo(2, ObjectType.MNTNER, "SSO-MNT")));
        when(updateDao.getAttributeReference(AttributeType.MNT_REF, CIString.ciString("SSO-MNT"))).thenReturn(new RpslObjectInfo(3, ObjectType.ORGANISATION, "ORG-TOL-TEST"));
        final RpslObject org = RpslObject.parse("organisation: ORG-TOL-TEST");
        when(objectDao.getById(3)).thenReturn(org);

        final Set<RpslObject> organisationsForUser = subject.findOrganisationsForAuth("SSO test@ripe.net");
        assertThat(organisationsForUser, contains(org));
    }
}
