package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class UserTest {
    @Test
    public void isValidPassword_single() {
        final User user = User.createWithPlainTextPassword("user", "password");

        assertThat(user.isValidPassword("password"), is(true));
        assertThat(user.isValidPassword(" password"), is(false));
        assertThat(user.isValidPassword("password "), is(false));
        assertThat(user.isValidPassword("PASSWORD"), is(false));
        assertThat(user.isValidPassword(""), is(false));
    }

    @Test
    public void no_objectTypes() {
        final User user = User.createWithPlainTextPassword("user", "password");

        assertThat(user.getObjectTypes(), hasSize(0));
    }

    @Test
    public void multiple_objectTypes() {
        final User user = User.createWithPlainTextPassword("user", "password", ObjectType.INETNUM, ObjectType.INET6NUM);

        assertThat(user.getObjectTypes(), containsInAnyOrder(ObjectType.INETNUM, ObjectType.INET6NUM));
    }
}
