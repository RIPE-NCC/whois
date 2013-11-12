package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AsBlockRangeTest {
    @Test
    public void validAsBlockRanges() {
        checkAsBlockRange(AsBlockRange.parse("AS1-AS2"), 1, 2);
        checkAsBlockRange(AsBlockRange.parse("as1-as2"), 1, 2);
        checkAsBlockRange(AsBlockRange.parse("AS1 -AS2"), 1, 2);
        checkAsBlockRange(AsBlockRange.parse("AS1 - AS2"), 1, 2);
        checkAsBlockRange(AsBlockRange.parse("AS1 - AS4294967295"), 1, 4294967295L);
        checkAsBlockRange(AsBlockRange.parse("AS1 - AS1"), 1, 1);
    }


    @Test
    public void validAsBlockContains() {
        assertTrue(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS110-AS130")));
        assertTrue(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS100-AS130")));
        assertTrue(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS100-AS200")));
        assertFalse(AsBlockRange.parse("AS10-AS20").contains(AsBlockRange.parse("AS2-AS50")));
        assertTrue(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS150-AS200")));
        assertFalse(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS201-AS300")));
        assertFalse(AsBlockRange.parse("AS10-AS20").contains(AsBlockRange.parse("AS1 - AS4294967295")));
        assertTrue(AsBlockRange.parse("AS1 - AS4294967295").contains(AsBlockRange.parse("AS2-AS50")));
    }

    @Test
    public void validAsBlockEquals() {
        assertTrue(AsBlockRange.parse("AS100-AS200").equals(AsBlockRange.parse("AS100-AS200")));
        assertFalse(AsBlockRange.parse("AS100-AS200").equals(AsBlockRange.parse("AS100-AS201")));
        assertFalse(AsBlockRange.parse("AS150-AS250").equals(AsBlockRange.parse("AS100-AS200")));
        assertFalse(AsBlockRange.parse("AS101-AS200").equals(AsBlockRange.parse("AS100-AS200")));
        assertTrue(AsBlockRange.parse("AS1 - AS4294967295").equals(AsBlockRange.parse("AS1 - AS4294967295")));
    }

    @Test(expected = AttributeParseException.class)
    public void invalidRange() {
        AsBlockRange.parse("AS2 - AS1");
    }

    @Test(expected = AttributeParseException.class)
    public void nonNumericAsBlockSingleArgument() {
        AsBlockRange.parse("ASx");
    }

    @Test(expected = AttributeParseException.class)
    public void singleAsBlockWithSeparator() {
        AsBlockRange.parse("AS1-");
    }

    @Test(expected = AttributeParseException.class)
    public void nonNumericAsBlockRangeFirstArgument() {
        AsBlockRange.parse("ASx-AS1");
    }

    @Test(expected = AttributeParseException.class)
    public void nonNumericAsBlockRangeSecondArgument() {
        AsBlockRange.parse("AS1-ASx");
    }

    @Test(expected = AttributeParseException.class)
    public void asBlockRangeThirdArgument() {
        AsBlockRange.parse("AS1-AS2-AS3");
    }

    @Test(expected = AttributeParseException.class)
    public void asBlockArgumentWithoutPrefix() {
        AsBlockRange.parse("1-2");
    }

    @Test(expected = AttributeParseException.class)
    public void emptyAsBlockRangeString() {
        AsBlockRange.parse("");
    }

    private void checkAsBlockRange(AsBlockRange range, long begin, long end) {
        assertTrue(range.getBegin() == begin);
        assertTrue(range.getEnd() == end);
    }
}
