package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ObjectKeyTest {
    @Test
    public void equals() {
        final ObjectKey subject = new ObjectKey(ObjectType.MNTNER, "key");

        assertThat(subject.equals(null), is(false));
        assertThat(subject.equals(""), is(false));
        assertThat(subject.equals(subject), is(true));
        assertThat(subject.equals(new ObjectKey(ObjectType.MNTNER, "key")), is(true));
        assertThat(subject.equals(new ObjectKey(ObjectType.MNTNER, "KEY")), is(true));
        assertThat(subject.equals(new ObjectKey(ObjectType.MNTNER, "key2")), is(false));
        assertThat(subject.equals(new ObjectKey(ObjectType.ORGANISATION, "key")), is(false));
    }

    @Test
    public void hash() {
        final ObjectKey subject = new ObjectKey(ObjectType.MNTNER, "key");
        assertThat(subject.hashCode(), is(new ObjectKey(ObjectType.MNTNER, "key").hashCode()));
        assertThat(subject.hashCode(), not(is(new ObjectKey(ObjectType.ORGANISATION, "key").hashCode())));
    }

    @Test
    public void hash_uppercase() {
        final ObjectKey subject = new ObjectKey(ObjectType.MNTNER, "key");
        assertThat(subject.hashCode(), is(new ObjectKey(ObjectType.MNTNER, "KEY").hashCode()));
    }

    @Test
    public void string() {
        final ObjectKey subject = new ObjectKey(ObjectType.MNTNER, "key");
        assertThat(subject.toString(), is("[mntner] key"));
    }

    @Test
    public void string_uppercase() {
        final ObjectKey subject = new ObjectKey(ObjectType.MNTNER, "KeY");
        assertThat(subject.toString(), is("[mntner] KeY"));
    }

}
