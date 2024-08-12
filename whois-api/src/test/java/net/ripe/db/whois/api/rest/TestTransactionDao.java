package net.ripe.db.whois.api.rest;

import net.ripe.db.nrtm4.dao.NrtmKeyConfigDao;
import net.ripe.db.nrtm4.generator.NrtmKeyPairService;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectDao;
import net.ripe.db.whois.common.domain.BlockEvent;
import net.ripe.db.whois.common.mail.EmailStatus;
import net.ripe.db.whois.common.TransactionConfiguration;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Repository
public class TestTransactionDao {

    @Autowired
    private TestDateTimeProvider testDateTimeProvider;

    @Autowired
    private EmailStatusDao emailStatusDao;

    @Autowired
    JdbcRpslObjectDao jdbcRpslObjectDao;

    @Autowired
    NrtmKeyPairService nrtmKeyPairService;

    @Autowired
    NrtmKeyConfigDao nrtmKeyConfigDao;

    @Autowired
    DatabaseHelper databaseHelper;

    @Transactional
    public void testTransactionSourceAware() {
        databaseHelper.addObject("person: Test Person\nnic-hdl: TP1-TEST");

        throw new RuntimeException("This should rollback");
    }

    @Transactional(transactionManager = TransactionConfiguration.ACL_UPDATE_TRANSACTION_MANAGER)
    public void testTransactionACL() {
        databaseHelper.getAclTemplate().update(
                "INSERT INTO acl_sso_event (sso_id, event_time, daily_limit, event_type) VALUES (?, ?, ?, ?)",
                "testSSOId",
                testDateTimeProvider.getCurrentDate(),
                100,
                BlockEvent.Type.BLOCK_PERMANENTLY
        );

        assertThat(getAclSSOCount(), is(1));

        throw new RuntimeException("This should rollback");
    }

    @Transactional(transactionManager = TransactionConfiguration.NRTM_UPDATE_TRANSACTION)
    public void testTransactionNrtmv4() {
        nrtmKeyPairService.generateKeyRecord(true);
        assertThat(nrtmKeyConfigDao.isActiveKeyPairExists(), is(true));

        throw new RuntimeException("This should rollback");
    }

    @Transactional(transactionManager = TransactionConfiguration.INTERNALS_UPDATE_TRANSACTION)
    public void testTransactionInternals(final String email) {
        emailStatusDao.createEmailStatus(email, EmailStatus.UNDELIVERABLE);
        assertThat(emailStatusDao.getEmailStatus(Collections.singleton(email)).containsKey(email), is(true));

        throw new RuntimeException("This should rollback");
    }

    public @Nullable Integer getAclSSOCount() {
        return databaseHelper.getAclTemplate().queryForObject("Select count(*) from acl_sso_event", Integer.class);
    }
}
