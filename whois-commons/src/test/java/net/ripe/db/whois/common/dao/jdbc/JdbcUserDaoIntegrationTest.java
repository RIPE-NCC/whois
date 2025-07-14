package net.ripe.db.whois.common.dao.jdbc;


import net.ripe.db.whois.common.dao.UserDao;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
public class JdbcUserDaoIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired UserDao subject;

    @Test
    public void getOverrideUser_not_found() {
        assertThrows(EmptyResultDataAccessException.class, () -> {
            subject.getOverrideUser("unexist");
        });
    }

    @Test
    public void getOverrideUser_no_objectTypes() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("test", "password"));

        final User user = subject.getOverrideUser("test");
        assertThat(user.getUsername(), is(ciString("test")));
        assertThat(user.getHashedPassword(), is("5f4dcc3b5aa765d61d8327deb882cf99"));
        assertThat(user.getObjectTypes(), hasSize(0));
    }

    @Test
    public void getOverrideUser_multiple_objectTypes() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("test", "password", ObjectType.INETNUM, ObjectType.INET6NUM));

        final User user = subject.getOverrideUser("test");
        assertThat(user.getUsername(), is(ciString("test")));
        assertThat(user.getHashedPassword(), is("5f4dcc3b5aa765d61d8327deb882cf99"));
        assertThat(user.getObjectTypes(), containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void getOverrideUser_multiple_objectTypes_with_underscore() {
        databaseHelper.insertUser(User.createWithPlainTextPassword("test", "password", ObjectType.AUT_NUM, ObjectType.INET_RTR));

        final User user = subject.getOverrideUser("test");
        assertThat(user.getUsername(), is(ciString("test")));
        assertThat(user.getHashedPassword(), is("5f4dcc3b5aa765d61d8327deb882cf99"));
        assertThat(user.getObjectTypes(), containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INET_RTR));
    }
}
