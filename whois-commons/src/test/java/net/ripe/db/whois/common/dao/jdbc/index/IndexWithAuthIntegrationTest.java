package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

@Tag("IntegrationTest")
public class IndexWithAuthIntegrationTest extends IndexIntegrationTestBase {

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     PP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    Map<RpslObject,RpslObjectUpdateInfo> objectUpdateInfoMap;
    IndexWithAuth subject;

    @BeforeEach
    public void startupWhoisServer() throws Exception {
        subject = new IndexWithAuth(AttributeType.AUTH, "auth", "auth");
        objectUpdateInfoMap = databaseHelper.addObjects(PAULETH_PALTHEN, OWNER_MNT);
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
