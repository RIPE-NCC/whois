package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.domain.CIString;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RpslObjectTest {
    private final String maintainer = "" +
            "mntner:          DEV-MNT\n" +
            "descr:           DEV maintainer\n" +
            "admin-c:         VM1-DEV\n" +
            "tech-c:          VM1-DEV\n" +
            "upd-to:          v.m@example.net\n" +
            "mnt-nfy:         auto@example.net\n" +
            "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
            "remarks:         password: secret\n" +
            "mnt-by:      DEV-MNT\n" +
            "referral-by:     DEV-MNT\n" +
            "changed:         BECHA@example.net 20101010\n" +
            "source:          DEV\n";

    private RpslObject subject;

    @Test(expected = IllegalArgumentException.class)
    public void parseNullFails() {
        RpslObject.parse((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmptyFails() {
        RpslObject.parse(new byte[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmptyStringFails() {
        parseAndAssign("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNullSizedKey() {
        parseAndAssign(":");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidCharacterInKey() {
        parseAndAssign(" :");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmptyLineBeforeObject() {
        parseAndAssign("\nk:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseEmptyLineAfterObject() {
        parseAndAssign("k:\n\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void attributeKeyCannotBeNull() {
        new RpslAttribute((String)null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void attributeValueCannotBeNull() {
        new RpslAttribute("", (String)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkAllowedCharactersInKey() {
        parseAndAssign("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-:");
    }

    @Test
    public void checkThatSpecialCharactersGetThrough() {
        parseAndAssign("person:  New Test Person\n" +
                "address: Flughafenstraße 120\n" +
                "address: D - 40474 Düsseldorf\n" +
                "nic-hdl: ABC-RIPE\n");
        assertThat(subject.toString(), containsString("Flughafenstraße"));
        assertThat(subject.toString(), containsString("Düsseldorf"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInValidMultiKeyObject() {
        parseAndAssign("k:\nk");
    }

    @Test
    public void parseValidSmallestObject() {
        parseAndAssign("mt:DEV-MNT");

        assertThat(subject, is(not(nullValue())));
        assertThat(subject.getType(), is(ObjectType.MNTNER));
        Assert.assertTrue(subject.containsAttribute(AttributeType.MNTNER));
        assertThat(subject.getValueForAttribute(AttributeType.MNTNER).toString().toString(), is("DEV-MNT"));
    }

    @Test
    public void parseGarbageValue() {
        String key = "mntner";
        String value = ":#!@#$%^&*()_+~![]{};':<>,./?\\";
        parseAndAssign(key + ":" + value);

        Assert.assertTrue(subject.containsAttribute(AttributeType.MNTNER));
        assertThat(subject.findAttributes(AttributeType.MNTNER), hasSize(1));
        assertThat(subject.findAttributes(AttributeType.MNTNER).get(0).getValue(), is(value));
    }

    @Test
    public void parseValidObjectWithValue() {
        String key = "mntner";
        String value = "value";

        parseAndAssign(key + ":" + value);

        Assert.assertTrue(subject.containsAttribute(AttributeType.MNTNER));
        assertThat(subject.findAttributes(AttributeType.MNTNER), hasSize(1));
        assertThat(subject.findAttributes(AttributeType.MNTNER).get(0).getValue(), is(value));
    }

    @Test
    public void parseContinuationLines() {
        String key = "descr";
        String value = "\n+1\n 2\n\t3";

        parseAndAssign("mntner: DEV-MNT\n" + key + ":" + value + "\n" + key + ":" + value);

        Assert.assertTrue(subject.containsAttribute(AttributeType.MNTNER));
        Assert.assertTrue(subject.containsAttribute(AttributeType.DESCR));
        assertThat(subject.findAttributes(AttributeType.MNTNER), hasSize(1));
        assertThat(subject.findAttributes(AttributeType.DESCR), hasSize(2));
        assertThat(subject.findAttributes(AttributeType.DESCR).get(0).getValue(), is(value));
    }

    @Test
    public void parseMultipleIdenticalKeys() {
        String key = "descr";
        int amount = 1000;

        parseAndAssign("mntner: DEV-MNT\n" + StringUtils.repeat(key + ": value", "\n", amount));

        assertThat(subject.findAttributes(AttributeType.DESCR), hasSize(amount));
    }

    private byte[] bytesFrom(String input) {
        return input.getBytes(Charsets.UTF_8);
    }

    private void parseAndAssign(String input) {
        subject = parse(input);
    }

    private RpslObject parse(String input) {
        return RpslObject.parse(bytesFrom(input));
    }

    @Test
    public void parseSingleObjectMaintainer() {
        parseAndAssign(maintainer);
        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "descr:          DEV maintainer\n" +
                "admin-c:        VM1-DEV\n" +
                "tech-c:         VM1-DEV\n" +
                "upd-to:         v.m@example.net\n" +
                "mnt-nfy:        auto@example.net\n" +
                "auth:           MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
                "remarks:        password: secret\n" +
                "mnt-by:         DEV-MNT\n" +
                "referral-by:    DEV-MNT\n" +
                "changed:        BECHA@example.net 20101010\n" +
                "source:         DEV\n"));

        assertThat(subject.findAttributes(AttributeType.MNTNER), hasSize(1));
        assertThat(subject.getValueForAttribute(AttributeType.MNTNER).toString(), is("DEV-MNT"));
        assertThat(subject.getValueForAttribute(AttributeType.SOURCE).toString(), is("DEV"));
    }

    @Test
    public void findAttributes_multiple() {
        parseAndAssign(maintainer);
        assertThat(subject.findAttributes(AttributeType.MNTNER, AttributeType.ADMIN_C, AttributeType.TECH_C), hasSize(3));
    }

    @Test
    public void testEquality() {
        parseAndAssign("mntner:DEV-TST-MNT\nsource:RIPE");
        assertThat(subject, is(subject));
        assertThat(subject.hashCode(), is(subject.hashCode()));

        Assert.assertFalse(subject.equals(null));
        Assert.assertFalse(subject.equals(1));

        final RpslObject subject2 = parse(subject.toString());
        assertThat(subject, is(subject2));
        assertThat(subject.hashCode(), is(subject2.hashCode()));

        final RpslObject subject3 = parse("mntner:DEV-TST-MNT\nsource:RIPE\nsource:RIPE");
        assertThat(subject, is(not(subject3)));
    }

    @Test
    public void testEquality_Attribute() {
        final RpslObject object = parse("mntner:DEV-TST-MNT\nsource:RIPE\nsource:RIPE2\nsource:RIPE\nauth:bar");
        final List<RpslAttribute> source = object.findAttributes(AttributeType.SOURCE);
        final RpslAttribute attribute = source.get(0);

        assertThat(attribute, is(attribute));
        assertThat(attribute.hashCode(), is(attribute.hashCode()));

        assertThat(attribute, is(not(source.get(1))));
        assertThat(attribute, is(source.get(2)));

        assertThat(attribute, is(not(object.findAttributes(AttributeType.AUTH).get(0))));
    }

    @Test
    public void getFormattedKey_simple() {
        final RpslObject subject = RpslObject.parse("mntner: DEV-ROOT-MNT");

        assertThat(subject.getFormattedKey().toString(), is("[mntner] DEV-ROOT-MNT"));
    }

    @Test
    public void getFormattedKey_composed() {
        final RpslObject subject = RpslObject.parse("route: 10/8\norigin: AS333");

        assertThat(subject.getFormattedKey().toString(), is("[route] 10/8AS333"));
    }

    @Test
    public void testNumberInKey() {
        RpslObject ro = parse("route6:         2001:0000::/32\norigin:AS10");
        assertThat(ro.getAttributes().get(0).getKey().toString(), is("route6"));
        assertThat(ro.findAttributes(AttributeType.ROUTE6), hasSize(1));
    }

    @Test
    public void testCasingAndNumberInKey() {
        RpslObject ro = parse("roUte6:         2001:0000::/32\norigin:AS10");
        assertThat(ro.getAttributes().get(0).getKey().toString(), is("route6"));
    }

    @Test
    public void testIfGetAttributesReturnAll() {
        String multiMnt = "mntner:          DEV-MNT\n" +
                "descr:           DEV maintainer\n" +
                "source:          RIPE";

        RpslObject ro = parse(multiMnt);
        assertThat(ro.getAttributes().size(), is(3));
    }

    @Test
    public void test_get_key_person() {
        parseAndAssign("person: foo \nnic-hdl: VM1-DEV  # SOME COMMENT \n");

        assertThat(subject.getKey().toString(), is("VM1-DEV"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_get_key_person_empty() {
        parseAndAssign("person: foo # Comment \n");
        subject.getKey();
    }

    @Test
    public void test_get_key_role() {
        parseAndAssign("role: foo \nnic-hdl: VM1-DEV # Comment\n");

        assertThat(subject.getKey().toString(), is("VM1-DEV"));
    }

    @Test
    public void test_get_key_maintainer() {
        parseAndAssign("mntner:          DEV-MNT  # Comment \n" +
                "descr:           DEV maintainer\n" +
                "admin-c:         VM1-DEV\n" +
                "tech-c:          VM1-DEV\n" +
                "upd-to:          v.m@example.net\n" +
                "mnt-nfy:         auto@example.net\n" +
                "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
                "remarks:         password: secret\n" +
                "mnt-by:      DEV-MNT\n" +
                "referral-by:     DEV-MNT\n" +
                "changed:         BECHA@example.net 20101010\n" +
                "source:          DEV\n");
        assertThat(subject.getKey().toString(), is("DEV-MNT"));
    }

    @Test
    public void test_get_key_route() {
        parseAndAssign("route: 10/8 # Comment \norigin: AS333 # Comment \n");
        assertThat(subject.getKey().toString(), is("10/8AS333"));
    }

    @Test
    public void test_get_key_route6() {
        parseAndAssign("route6: ::0/0 # Comment  \norigin: AS333 # Comment  \n");
        assertThat(subject.getKey().toString(), is("::0/0AS333"));
    }

    @Test
    public void containsAttribute_works() {
        parseAndAssign(maintainer);
        assertThat(subject.containsAttribute(AttributeType.SOURCE), is(true));
    }

    @Test
    public void containsAttribute_unknown() {
        parseAndAssign(maintainer);
        assertThat(subject.containsAttribute(AttributeType.PERSON), is(false));
    }

    @Test
    public void test() {
        assertThat(RpslObject.parse("mntner: mnt\n a"), is(RpslObject.parse("mntner: mnt\n\ta")));
    }

    @Test
    public void compareRpslObjects_single() {
        assertThat(RpslObject.parse("mntner: mnt"), is(RpslObject.parse("mntner: mnt")));
        assertThat(RpslObject.parse("mntner: mnt"), is(RpslObject.parse("mntner: mnt\n")));
        assertThat(RpslObject.parse("mntner: mnt"), is(not(RpslObject.parse("mntner: mnt\n\ta"))));
        assertThat(RpslObject.parse("mntner: mnt\n a"), is(RpslObject.parse("mntner: mnt\n\ta")));
        assertThat(RpslObject.parse("mntner: mnt\n    \t\t     a"), is(RpslObject.parse("mntner: mnt\n\ta")));
        assertThat(RpslObject.parse("mntner: \t  mnt  \t \n \t     a"), is(RpslObject.parse("mntner: mnt\n\ta")));
        assertThat(RpslObject.parse("mntner: \t  one \t two \t \n \t     a"), is(RpslObject.parse("mntner: one two\n\ta")));
        assertThat(RpslObject.parse("mntner:one \t two \t \n \t     a"), is(RpslObject.parse("mntner: one two\n\ta")));
        assertThat(RpslObject.parse("mntner: mnt # comment"), is(RpslObject.parse("mntner: mnt")));
        assertThat(RpslObject.parse("mntner: mnt # comment"), is(RpslObject.parse("mntner: mnt # comment")));
        assertThat(RpslObject.parse("mntner: mnt# comment"), is(RpslObject.parse("mntner: mnt # comment")));
        assertThat(RpslObject.parse("mntner: mnt # com  ment"), is(RpslObject.parse("mntner: mnt # com ment")));
        assertThat(RpslObject.parse("mntner: mnt # com  ment"), is(RpslObject.parse("mntner: mnt #com ment")));
        assertThat(RpslObject.parse("mntner: mnt two three four"), is(RpslObject.parse("mntner: mnt # one\n two\n+ three\n\tfour")));
    }

    @Test
    public void compareRpslObjects_multiple() {
        assertThat(RpslObject.parse("mntner: mnt\nsource: RIPE"), is(RpslObject.parse("mntner: mnt\nsource:  RIPE\n")));
        assertThat(RpslObject.parse("mntner: mnt\nsource: RIPE"), is(RpslObject.parse("mntner: mnt\nsource:\tRIPE\n")));
        assertThat(RpslObject.parse("mntner: mnt\n+one\nsource: RIPE"), is(RpslObject.parse("mntner: mnt\n\tone\nsource:\tRIPE\n")));
        assertThat(RpslObject.parse("mntner: mnt\n+#one\nsource: RIPE"), is(RpslObject.parse("mntner: mnt\n\t #two\nsource:\tRIPE\n")));
    }

    @Test
    public void object_with_CR() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:          DEV-MNT\r\n" +
                "descr:           DEV maintainer\r\n" +
                "admin-c:         VM1-DEV\r\n" +
                "tech-c:          VM1-DEV\r\n" +
                "upd-to:          v.m@example.net\r\n" +
                "mnt-nfy:         auto@example.net\r\n" +
                "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\r\n" +
                "remarks:         password: secret\r\n" +
                "mnt-by:      DEV-MNT\r\n" +
                "referral-by:     DEV-MNT\r\n" +
                "changed:         BECHA@example.net 20101010\r\n" +
                "source:          DEV\r\n");

        assertThat(object.toString(), not(containsString("\r")));
        assertThat(object.getKey().toString(), is("DEV-MNT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getValueForAttributeNone() {
        RpslObject.parse("mntner: DEV-MNT\n").getValueForAttribute(AttributeType.MNT_BY);
    }

    @Test
    public void getValueForAttribute() {
        assertThat(RpslObject.parse("mntner: DEV-MNT\n").getValueForAttribute(AttributeType.MNTNER).toString(), is("DEV-MNT"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getValueForAttributeMultiple() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT5\n" +
                "mnt-by: DEV-MNT4, DEV-MNT4\n" +
                "mnt-by: DEV-MNT3, DEV-MNT2\n" +
                "mnt-by: DEV-MNT1, DEV-MNT2\n");

        object.getValueForAttribute(AttributeType.MNT_BY);
    }

    @Test
    public void getValuesForAttribute() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by: DEV-MNT5\n" +
                "mnt-by: DEV-MNT4, DEV-MNT4\n" +
                "mnt-by: DEV-MNT3, DEV-MNT2\n" +
                "mnt-by: DEV-MNT1, DEV-MNT2\n");

        assertThat(convertToString(object.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT5", "DEV-MNT4", "DEV-MNT3", "DEV-MNT2", "DEV-MNT1"));
        assertThat(object.getValuesForAttribute(AttributeType.ADMIN_C), hasSize(0));
    }

    @Test
    public void cleanvalue_comparator_different_order() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT3,DEV-MNT1,DEV-MNT2");

        assertThat(object1, not(is(object2)));
    }

    @Test
    public void cleanvalue_comparator_different_values() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT4");

        assertThat(object1, not(is(object2)));
    }

    @Test
    public void cleanvalue_comparator_same_order() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");

        assertThat(object1, is(object2));
    }

    @Test
    public void cleanvalue_comparator_same_order_different_casing() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("MNTNER: dev-mnt\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");

        assertThat(object1, is(object2));
    }

    @Test
    public void cleanvalue_comparator_same_order_different_key() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("poem: dev-mnt\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");

        assertThat(object1, not(is(object2)));
    }

    @Test
    public void cleanvalue_comparator_different_order_same_attributes() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1\nnic-hdl:NIC-RIPE\nmnt-by:DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("mntner: DEV-MNT\nnic-hdl:NIC-RIPE\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");

        assertThat(object1, not(is(object2)));
    }

    @Test
    public void cleanvalue_comparator_subset() {
        final RpslObject object1 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT2,DEV-MNT3");
        final RpslObject object2 = RpslObject.parse("mntner: DEV-MNT\nmnt-by:DEV-MNT1,DEV-MNT3");

        assertThat(object1, not(is(object2)));
    }

    @Test
    public void space_in_attribute_key() {
        final RpslObject object = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by :DEV-MNT1,DEV-MNT2,DEV-MNT3");

        assertThat(object.findAttributes(AttributeType.MNT_BY), hasSize(0));
        assertNull(object.getAttributes().get(1).getType());
    }

    @Test
    public void continuation_line_with_space() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                " DEV-MNT2,\n" +
                " DEV-MNT3,\n" +
                " DEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "                DEV-MNT2,\n" +
                "                DEV-MNT3,\n" +
                "                DEV-MNT4\n"));
    }

    @Test
    public void continuation_line_with_space_and_plus_in_value() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   +DEV+MNT1,\n" +
                " +DEV+MNT2,\n" +
                " +DEV+MNT3,\n" +
                " +DEV+MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("+DEV+MNT1", "+DEV+MNT2", "+DEV+MNT3", "+DEV+MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         +DEV+MNT1,\n" +
                "                +DEV+MNT2,\n" +
                "                +DEV+MNT3,\n" +
                "                +DEV+MNT4\n"));
    }

    @Test
    public void continuation_line_poorly_formatted() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                "                    DEV-MNT2,\n" +
                "    DEV-MNT3,\n" +
                "                                   DEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "                DEV-MNT2,\n" +
                "                DEV-MNT3,\n" +
                "                DEV-MNT4\n"));
    }

    @Test
    public void continuation_line_with_tab() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                "\tDEV-MNT2,\n" +
                "\tDEV-MNT3,\n" +
                "\tDEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "                DEV-MNT2,\n" +
                "                DEV-MNT3,\n" +
                "                DEV-MNT4\n"));
    }

    @Test
    public void continuation_line_with_tab_poorly_formatted() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:    DEV-MNT1,\n" +
                "\t                                     \tDEV-MNT2,\n" +
                "\t\t\t\t\t\t\t\t\t\t\t\t\t\tDEV-MNT3,\n" +
                "\tDEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "                DEV-MNT2,\n" +
                "                DEV-MNT3,\n" +
                "                DEV-MNT4\n"));
    }

    @Test
    public void continuation_line_with_plus() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                "+DEV-MNT2,\n" +
                "+DEV-MNT3,\n" +
                "+DEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "+               DEV-MNT2,\n" +
                "+               DEV-MNT3,\n" +
                "+               DEV-MNT4\n"));
    }

    @Test
    public void continuation_line_with_spaces_only() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                "          \n" +
                "          DEV-MNT2,\n" +
                "          DEV-MNT3");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "\n" +
                "                DEV-MNT2,\n" +
                "                DEV-MNT3\n"));
    }

    @Test
    public void continuation_line_with_plus_in_value() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   +DEV+MNT1,\n" +
                "++DEV+MNT2,\n" +
                "+ +DEV+MNT3,\n" +
                "+  +DEV+MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("+DEV+MNT1", "+DEV+MNT2", "+DEV+MNT3", "+DEV+MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         +DEV+MNT1,\n" +
                "+               +DEV+MNT2,\n" +
                "+               +DEV+MNT3,\n" +
                "+               +DEV+MNT4\n"));
    }

    @Test
    public void continuation_line_with_plus_poorly_formatted() {
        final RpslObject subject = RpslObject.parse("" +
                "mntner: DEV-MNT\n" +
                "mnt-by:   DEV-MNT1,\n" +
                "+            DEV-MNT2,\n" +
                "+ DEV-MNT3,\n" +
                "+                                          DEV-MNT4");

        assertThat(convertToString(subject.getValuesForAttribute(AttributeType.MNT_BY)), contains("DEV-MNT1", "DEV-MNT2", "DEV-MNT3", "DEV-MNT4"));

        assertThat(subject.toString().toString(), is("" +
                "mntner:         DEV-MNT\n" +
                "mnt-by:         DEV-MNT1,\n" +
                "+               DEV-MNT2,\n" +
                "+               DEV-MNT3,\n" +
                "+               DEV-MNT4\n"));
    }

    @Test
    public void diff_idential() {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: descr\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, original), is(""));
    }

    @Test
    public void diff_delete_lines() throws Exception {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: descr\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");
        final RpslObject updated = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, updated),
                is("@@ -1,4 +1,2 @@\n" +
                        " mntner:         UPD-MNT\n" +
                        "-description:    descr\n" +
                        "-mnt-by:         UPD-MNT\n" +
                        " source:         TEST\n"));
    }

    @Test
    public void diff_add_lines() {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "source: TEST\n");
        final RpslObject updated = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: descr\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, updated),
                is("@@ -1,2 +1,4 @@\n" +
                        " mntner:         UPD-MNT\n" +
                        "+description:    descr\n" +
                        "+mnt-by:         UPD-MNT\n" +
                        " source:         TEST\n"));
    }

    @Test
    public void diff_change_line() {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: descr\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");
        final RpslObject updated = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: updated\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, updated),
                is("@@ -1,3 +1,3 @@\n" +
                        " mntner:         UPD-MNT\n" +
                        "-description:    descr\n" +
                        "+description:    updated\n" +
                        " mnt-by:         UPD-MNT\n"));
    }

    @Test
    public void diff_add_and_change_lines() {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n");
        final RpslObject updated = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: updated\n" +
                "mnt-by: UPD-MNT2\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, updated),
                is("@@ -1,3 +1,4 @@\n" +
                        " mntner:         UPD-MNT\n" +
                        "-mnt-by:         UPD-MNT\n" +
                        "+description:    updated\n" +
                        "+mnt-by:         UPD-MNT2\n" +
                        " source:         TEST\n"));
    }

    @Test
    public void diff_separate_changes() {
        final RpslObject original = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: descr\n" +
                "admin-c: OR1-TEST\n" +
                "tech-c: OR2-TEST\n" +
                "remarks: remark\n" +
                "mnt-by: UPD-MNT\n" +
                "changed: noreply@ripe.net\n" +
                "source: TEST\n");
        final RpslObject updated = RpslObject.parse(
                "mntner: UPD-MNT\n" +
                "description: updated\n" +
                "admin-c: OR1-TEST\n" +
                "tech-c: OR2-TEST\n" +
                "remarks: remark\n" +
                "mnt-by: UPD-MNT\n" +
                "changed: updated@ripe.net\n" +
                "source: TEST\n");

        assertThat(RpslObjectFilter.diff(original, updated),
                is("@@ -1,3 +1,3 @@\n" +
                        " mntner:         UPD-MNT\n" +
                        "-description:    descr\n" +
                        "+description:    updated\n" +
                        " admin-c:        OR1-TEST\n" +
                        "@@ -6,3 +6,3 @@\n" +
                        " mnt-by:         UPD-MNT\n" +
                        "-changed:        noreply@ripe.net\n" +
                        "+changed:        updated@ripe.net\n" +
                        " source:         TEST\n"));
    }

    @Test
    public void parse_greek_attribute_value() {
        final RpslObject rpslObject = RpslObject.parse(
                "person:  Test Person\n" +
                "address: Καλημέρα κόσμε\n" +
                "phone:   +31 20 123456\n" +
                "nic-hdl: AUTO-1\n" +
                "mnt-by:  TEST-DBM-MNT\n" +
                "changed: hostmaster@ripe.net\n" +
                "source:  TEST\n");

        assertThat(rpslObject.findAttribute(AttributeType.ADDRESS).getValue(), containsString("Καλημέρα κόσμε"));
    }

    @Test
    public void parse_japanese_attribute_value() {
        final RpslObject rpslObject = RpslObject.parse(
                "person:  Test Person\n" +
                "address: こんにちは 世界\n" +
                "phone:   +31 20 123456\n" +
                "nic-hdl: AUTO-1\n" +
                "mnt-by:  TEST-DBM-MNT\n" +
                "changed: hostmaster@ripe.net\n" +
                "source:  TEST\n");

        assertThat(rpslObject.findAttribute(AttributeType.ADDRESS).getValue(), containsString("こんにちは 世界"));
    }

    @Test
    public void parse_arabic_attribute_value() {
        final RpslObject rpslObject = RpslObject.parse(
                "person:  Test Person\n" +
                "address: حد بيتكلم انجليزي؟\n" +
                "phone:   +31 20 123456\n" +
                "nic-hdl: AUTO-1\n" +
                "mnt-by:  TEST-DBM-MNT\n" +
                "changed: hostmaster@ripe.net\n" +
                "source:  TEST\n");

        assertThat(rpslObject.findAttribute(AttributeType.ADDRESS).getValue(), containsString("حد بيتكلم انجلي"));
    }

    private static Iterable<String> convertToString(final Iterable<CIString> c) {
        return Iterables.transform(c, new Function<CIString, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final CIString input) {
                if (input == null) {
                    return null;
                }

                return input.toString();
            }
        });
    }
}
