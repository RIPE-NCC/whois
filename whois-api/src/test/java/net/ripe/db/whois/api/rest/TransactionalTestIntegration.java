package net.ripe.db.whois.api.rest;

import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

//TODO: Remove this test once we are sure transactions work
@Tag("IntegrationTest")
public class TransactionalTestIntegration extends AbstractIntegrationTest {

    @Autowired
    private TestDateTimeProvider testDateTimeProvider;

    @Autowired
    TestTransactionDao testTransactionDao;

    @Autowired
    NrtmKeyPairService nrtmKeyPairService;

    @Autowired
    NrtmKeyConfigDao nrtmKeyConfigDao;

    @Autowired
    private EmailStatusDao emailStatusDao;

    @Autowired
    JdbcRpslObjectDao jdbcRpslObjectDao;

    @BeforeEach
    public void setup() {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-04T17:00:00"));
    }

    @Test
    public void test_transaction_nrtmv4() {
        assertThat(nrtmKeyConfigDao.isActiveKeyPairExists(), is(false));

        try {
            testTransactionDao.testTransactionNrtmv4();
        } catch (Exception e) {}

        assertThat(nrtmKeyConfigDao.isActiveKeyPairExists(), is(false));
    }

    @Test
    public void test_transaction_internals() {
        final String email = "test123@ripe.net";
        try {
            testTransactionDao.testTransactionInternals(email);
        } catch (Exception e) {}

        assertThat(emailStatusDao.getEmailStatus(Collections.singleton(email)).containsKey(email), is(false));
    }

    @Test
    public void test_transaction_acl() {
        assertThat(testTransactionDao.getAclSSOCount(), is(0));

        try {
            testTransactionDao.testTransactionACL();
        } catch (Exception e) {}

        assertThat(testTransactionDao.getAclSSOCount(), is(0));
    }

    @Test
    public void test_transaction_sourceAware() {
        try {
            testTransactionDao.testTransactionSourceAware();
        } catch (Exception e) {}

        assertThat(jdbcRpslObjectDao.getByKeyOrNull(ObjectType.PERSON, "TP1-TEST"), nullValue());
    }
}