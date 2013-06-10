package net.ripe.db.whois.update.domain;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UpdateMessagesTest {

    @Test
    public void authentication_failed_empty_list() {
        RpslObject object = RpslObject.parse("person: New Person\nnic-hdl: AUTO-1");

        final Message result = UpdateMessages.authenticationFailed(object, AttributeType.MNT_BY, Lists.<RpslObject>newArrayList());

        assertThat(result.toString(), is(
                "Authorisation for [person] AUTO-1 failed\nusing \"mnt-by:\"\nno valid maintainer found\n"));
    }

    @Test
    public void authentication_failed_with_list() {
        RpslObject object = RpslObject.parse("person: New Person\nnic-hdl: AUTO-1\nmnt-by: maintainer");
        RpslObject mntner = RpslObject.parse("mntner: maintainer");

        final Message result = UpdateMessages.authenticationFailed(object, AttributeType.MNT_BY, Lists.newArrayList(mntner));

        assertThat(result.toString(), is(
                "Authorisation for [person] AUTO-1 failed\nusing \"mnt-by:\"\nnot authenticated by: maintainer"));
    }
}
