package net.ripe.db.whois.common.jdbc;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabaseVersionCheckTest {

    @Test
    public void testVersionNumberCheck() {
        assertThat(DatabaseVersionCheck.compareVersions("1.0", "2.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.1", "2.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.01", "2.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.2.3.4", "2.0.1"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("10.0", "2.0"), greaterThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("12.12.12.12.12.12", "11.99.2"), greaterThan(0));

        assertThat(DatabaseVersionCheck.compareVersions("2.0", "2.0.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("2.0", "2.0.0.1"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("2.0.0", "2.0.0.1"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("2.0.0.0", "2.0.0.1"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("2.0.0.0.0.0.0.0", "2.0.0.1"), lessThan(0));

        assertThat(DatabaseVersionCheck.compareVersions("1.0-1", "1.0-2"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.1-99", "2.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.01-0.11", "2.0"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.01-0.11", "1.1-1"), lessThan(0));
        assertThat(DatabaseVersionCheck.compareVersions("1.01-0.11", "1.1-00.12"), lessThan(0));
    }

    @Test
    public void testResourceMatcher() {
        assertThat(DatabaseVersionCheck.RESOURCE_MATCHER.matcher("whois-1.52.12").matches(), is(true));
        assertThat(DatabaseVersionCheck.RESOURCE_MATCHER.matcher("acl-2").matches(), is(true));
        assertThat(DatabaseVersionCheck.RESOURCE_MATCHER.matcher("lee7-2.5.4").matches(), is(false));
        assertThat(DatabaseVersionCheck.RESOURCE_MATCHER.matcher("lee7-2.5.4-1.2").matches(), is(false));
    }

    @Test
    public void testCheckDatabaseOK() {
        final DatabaseVersionCheck subject = new DatabaseVersionCheck(null);
        subject.checkDatabase(ImmutableList.of("whois-1.51", "whois-1.4", "whois-2.15.4"), "TEST", "whois-2.16");
    }

    @Test
    public void testCheckDatabaseFail() {
        assertThrows(IllegalStateException.class, () -> {
            DatabaseVersionCheck subject = new DatabaseVersionCheck(null);
            subject.checkDatabase(ImmutableList.of("whois-1.51", "whois-1.4", "whois-2.15.4"), "TEST", "whois-1.16");

        });
    }

    @Test
    public void testCheckDatabaseSucceedForAnotherDB() {
        final DatabaseVersionCheck subject = new DatabaseVersionCheck(null);
        subject.checkDatabase(ImmutableList.of("scheduler-1.51", "whois-1.4", "acl-2.15.4"), "TEST", "whois-1.16");
    }
}
