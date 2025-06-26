package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Tag("IntegrationTest")
public class IndexWithValueAndTypeIntegrationTest extends IndexIntegrationTestBase {

    @Test
    public void addToIndex() {
        IndexWithValueAndType subject = new IndexWithValueAndType(AttributeType.NOTIFY, "notify", "notify");
        RpslObjectInfo maintainer = new RpslObjectInfo(1, ObjectType.MNTNER, "MNT-TEST");

        final int added = subject.addToIndex(whoisTemplate, maintainer, null, "MNT-TEST");
        assertThat(added, is(1));
    }
}
