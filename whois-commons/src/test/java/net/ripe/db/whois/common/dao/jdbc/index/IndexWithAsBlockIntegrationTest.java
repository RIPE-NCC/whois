package net.ripe.db.whois.common.dao.jdbc.index;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Category(IntegrationTest.class)
public class IndexWithAsBlockIntegrationTest extends IndexIntegrationTestBase {
    private IndexWithAsBlock subject;

    private RpslObject asBlock;
    private RpslObjectInfo asBlockInfo;

    @Before
    public void setup() {
        subject = new IndexWithAsBlock(AttributeType.AS_BLOCK);

        asBlock = RpslObject.parse("as-block:AS31066-AS31066");
        asBlockInfo = new RpslObjectInfo(1, asBlock.getType(), asBlock.getKey());
    }

    @Test
    public void addToIndex() {
        subject.addToIndex(whoisTemplate, asBlockInfo, asBlock, asBlock.getTypeAttribute().getCleanValue());

        assertThat(whoisTemplate.queryForObject("SELECT COUNT(*) FROM as_block", Integer.class), is(1));
    }

    @Test
    public void findInIndex() {
        databaseHelper.addObject(asBlock);

        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, asBlockInfo.getKey());
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0).getKey(), is(asBlockInfo.getKey()));
    }

    @Test
    public void removeFromIndex() {
        databaseHelper.addObject(asBlock);

        subject.removeFromIndex(whoisTemplate, asBlockInfo);
        final List<RpslObjectInfo> infos = subject.findInIndex(whoisTemplate, asBlockInfo.getKey());
        assertThat(infos, hasSize(0));
    }
}
