package net.ripe.db.whois.internal.api.abusec;

import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class JdbcApiKeyDaoTest  extends AbstractInternalTest {

    @Autowired JdbcApiKeyDao subject;

    @Test
    public void getAll() {
        insert("/api/acl", "API-KEY-1");
        insert("/api/whois", "API-KEY-2");

        final Map<String, String> results = subject.getAll();

        assertThat(results.keySet(), hasSize(2));
        assertThat(results, hasEntry("/api/acl", "API-KEY-1"));
        assertThat(results, hasEntry("/api/whois", "API-KEY-2"));
    }

    // helper methods

    private void insert(final String uri, final String key) {
        databaseHelper.getAclTemplate().update("INSERT INTO apikeys (uri_prefix, apikey, comment) VALUES (?,?,'comment')", uri, key);
    }
}
