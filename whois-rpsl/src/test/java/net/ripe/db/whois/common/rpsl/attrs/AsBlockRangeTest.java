package net.ripe.db.whois.common.rpsl.attrs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


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
        assertThat(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS110-AS130")), is(true));
        assertThat(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS100-AS130")), is(true));
        assertThat(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS100-AS200")), is(true));
        assertThat(AsBlockRange.parse("AS10-AS20").contains(AsBlockRange.parse("AS2-AS50")), is(false));
        assertThat(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS150-AS200")), is(true));
        assertThat(AsBlockRange.parse("AS100-AS200").contains(AsBlockRange.parse("AS201-AS300")), is(false));
        assertThat(AsBlockRange.parse("AS10-AS20").contains(AsBlockRange.parse("AS1 - AS4294967295")), is(false));
        assertThat(AsBlockRange.parse("AS1 - AS4294967295").contains(AsBlockRange.parse("AS2-AS50")), is(true));
    }

    @Test
    public void validAsBlockEquals() {
        assertThat(AsBlockRange.parse("AS100-AS200").equals(AsBlockRange.parse("AS100-AS200")), is(true));
        assertThat(AsBlockRange.parse("AS100-AS200").equals(AsBlockRange.parse("AS100-AS201")), is(false));
        assertThat(AsBlockRange.parse("AS150-AS250").equals(AsBlockRange.parse("AS100-AS200")), is(false));
        assertThat(AsBlockRange.parse("AS101-AS200").equals(AsBlockRange.parse("AS100-AS200")), is(false));
        assertThat(AsBlockRange.parse("AS1 - AS4294967295").equals(AsBlockRange.parse("AS1 - AS4294967295")), is(true));
    }

    @Test
    public void invalidRange() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("AS2 - AS1");
        });
    }

    @Test
    public void nonNumericAsBlockSingleArgument() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("ASx");
        });
    }

    @Test
    public void singleAsBlockWithSeparator() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("AS1-");
        });
    }

    @Test
    public void nonNumericAsBlockRangeFirstArgument() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("ASx-AS1");
        });
    }

    @Test
    public void nonNumericAsBlockRangeSecondArgument() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("AS1-ASx");
        });
    }

    @Test
    public void asBlockRangeThirdArgument() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("AS1-AS2-AS3");
        });

    }

    @Test
    public void asBlockArgumentWithoutPrefix() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("1-2");
        });

    }

    @Test
    public void emptyAsBlockRangeString() {
        Assertions.assertThrows(AttributeParseException.class, () -> {
            AsBlockRange.parse("");
        });
    }

    private void checkAsBlockRange(final AsBlockRange range, final long begin, final long end) {
        assertThat(range.getBegin(), is(begin));
        assertThat(range.getEnd(), is(end));
    }
}
