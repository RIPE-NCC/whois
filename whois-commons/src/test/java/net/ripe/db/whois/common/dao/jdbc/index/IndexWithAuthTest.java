package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static net.ripe.db.whois.common.RpslObjectFixtures.OWNER_MNT;
import static net.ripe.db.whois.common.RpslObjectFixtures.PAULETH_PALTHEN;
import static net.ripe.db.whois.common.RpslObjectFixtures.TEST_PERSON;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class IndexWithAuthTest extends IndexTestBase {

    Map<RpslObject,RpslObjectUpdateInfo> objectUpdateInfoMap;
    IndexWithAuth subject;

    @Before
    public void startupWhoisServer() throws Exception {
        subject = new IndexWithAuth(AttributeType.AUTH, "auth", "auth");
        objectUpdateInfoMap = databaseHelper.addObjects(TEST_PERSON, PAULETH_PALTHEN, OWNER_MNT);
    }

    @Test
    public void addIndex_skips_writing_md5_hash() throws Exception {
        RpslObjectInfo maintainer = objectUpdateInfoMap.get(OWNER_MNT);

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "MD5-PW $1$saltsalt$hashhash");
        assertThat(added, is(1));

        assertThat(subject.findInIndex(whoisTemplate, "MD5-PW $1$saltsalt$hashhash"), is(empty()));
    }

    @Test
    public void addIndex_works_for_sso_hash() throws Exception {
        RpslObjectInfo maintainer = objectUpdateInfoMap.get(OWNER_MNT);

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "SSO 1234-5678");
        assertThat(added, is(1));

        assertThat(subject.findInIndex(whoisTemplate, "SSO 1234-5678"), hasSize(1));
    }

    @Test
    public void addIndex_works_for_keycert() throws Exception {
        RpslObjectInfo maintainer = objectUpdateInfoMap.get(OWNER_MNT);

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "PGPKEY-12345678");
        assertThat(added, is(1));

        assertThat(subject.findInIndex(whoisTemplate, "PGPKEY-12345678"), hasSize(1));
    }

    @Test
    public void addIndex_works_case_insensitive() throws Exception {
        RpslObjectInfo maintainer = objectUpdateInfoMap.get(OWNER_MNT);

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "PGPKEY-12345678");
        assertThat(added, is(1));

        assertThat(subject.findInIndex(whoisTemplate, "PGpKeY-12345678"), hasSize(1));
    }

}
