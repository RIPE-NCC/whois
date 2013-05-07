package net.ripe.db.whois.nrtm;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.nrtm.integration.AbstractNrtmIntegrationBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AccessControlListTest extends AbstractNrtmIntegrationBase {
    @Autowired @Qualifier("aclDataSource") DataSource aclDataSource;
    @Autowired AccessControlList subject;

    private void addMirror(final String prefix) {
        new JdbcTemplate(aclDataSource).update("INSERT INTO acl_mirror VALUES (?, 'test')", prefix);
    }

    @Test
    public void testLoadingOfACL() {
        addMirror("10.0.0.1/32");
        addMirror("10.10/16");
        addMirror("2001:610::/32");
        subject.reload();

        assertThat(true, is(subject.isMirror(InetAddresses.forString("10.0.0.1"))));
        assertThat(true, is(subject.isMirror(InetAddresses.forString("10.10.10.1"))));
        assertThat(false, is(subject.isMirror(InetAddresses.forString("0.0.0.0"))));
        assertThat(false, is(subject.isMirror(InetAddresses.forString("10.0.0.2"))));

        assertThat(true, is(subject.isMirror(InetAddresses.forString("2001:610:5fc::5"))));
        assertThat(true, is(subject.isMirror(InetAddresses.forString("2001:610::"))));
        assertThat(false, is(subject.isMirror(InetAddresses.forString("ffff::"))));
        assertThat(true, is(subject.isMirror(InetAddresses.forString("::1"))));
        assertThat(false, is(subject.isMirror(InetAddresses.forString("2001:5c1:670::3"))));
    }
}
