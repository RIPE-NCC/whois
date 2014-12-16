package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class RpslAttributeTest {
    private RpslAttribute subject;

    @Test
    public void remove_comments_single_line() {
        subject = new RpslAttribute("source", "    RIPE #");
        assertThat(subject.getCleanValue().toString(), is("RIPE"));
        assertThat(subject.getCleanComment(), equalTo(null));
    }

    @Test
    public void remove_comments_on_single_line() {
        subject = new RpslAttribute("source", "    RIPE # Some comment");
        assertThat(subject.getCleanValue().toString(), is("RIPE"));
        assertThat(subject.getCleanComment(), is("Some comment"));
    }

    @Test
    public void remove_comments_multiple_lines() {
        subject = new RpslAttribute("source", "    RIPE #\n RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE RIPE"));
        assertThat(subject.getCleanComment(), equalTo(null));
    }

    @Test
    public void remove_comments_multiple_lines_with_continuation() {
        subject = new RpslAttribute("source", "    RIPE #\n+ RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE RIPE"));
    }

    @Test
    public void remove_comments_with_plus() {
        subject = new RpslAttribute("source", "    RIPE + RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE + RIPE"));
    }

    @Test
    public void remove_comments_with_plusses() {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa"));
    }

    @Test
    public void remove_comments_with_plusses_and_data() {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+     asd");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa asd"));
    }

    @Test
    public void remove_comments_with_plusses_and_plus() {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+++     asd");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa ++ asd"));
    }

    @Test
    public void remove_comments_with_plusses_and() {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+asd\n");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa asd"));
    }

    @Test
    public void remove_comments_with_spaces() {
        subject = new RpslAttribute("domain", "foo       bar");
        assertThat(subject.getCleanValue().toString(), is("foo bar"));
    }

    @Test
    public void short_hand_plain() {
        subject = new RpslAttribute("domain", "foobar");
        assertThat(RpslAttributeFilter.getValueForShortHand(subject.getValue()), is(" foobar"));
    }

    @Test
    public void short_hand_spaced_prefix() {
        subject = new RpslAttribute("domain", "  foobar");
        assertThat(RpslAttributeFilter.getValueForShortHand(subject.getValue()), is(" foobar"));
    }

    @Test
    public void short_hand_any_continuation() {
        subject = new RpslAttribute("domain", "foo\n bar\n+qux\n\tzot");
        assertThat(RpslAttributeFilter.getValueForShortHand(subject.getValue()), is(" foo\n+ bar\n+ qux\n+ zot"));
    }

    @Test
    public void short_hand_only_touches_continuation_whitespace() {
        subject = new RpslAttribute("domain", "foo\t  #  \t\n bar\n+ qux\n\tzot");
        assertThat(RpslAttributeFilter.getValueForShortHand(subject.getValue()), is(" foo\t  #  \t\n+ bar\n+ qux\n+ zot"));
    }

    @Test
    public void short_hand_continuation_spaces() {
        subject = new RpslAttribute("domain", "foo\n           bar\n+         qux\n\t    zot");
        assertThat(RpslAttributeFilter.getValueForShortHand(subject.getValue()), is(" foo\n+ bar\n+ qux\n+ zot"));
    }

    @Test
    public void equals_is_case_insensitive() {
        subject = new RpslAttribute("remarks", "The quick brown fox.");
        assertThat(subject.equals(new RpslAttribute("remarks", "THE QUICK BROWN FOX.")), is(true));
    }

    @Test
    public void equals_unknown_type() {
        subject = new RpslAttribute("unknown", "The quick brown fox.");
        assertThat(subject.equals(new RpslAttribute("unknown", "THE QUICK BROWN FOX.")), is(true));
    }

    @Test
    public void equals_Integer() {
        subject = new RpslAttribute("remarks", "The quick brown fox.");
        assertFalse(subject.equals(1));
    }

    @Test
    public void equals_null() {
        subject = new RpslAttribute("remarks", "The quick brown fox.");
        assertThat(subject, not(is(nullValue())));
        assertFalse(subject.equals(null));

    }

    @Test
    public void equals_shorthand() {
        subject = new RpslAttribute("remarks", "The quick brown fox.");
        assertThat(subject.equals(new RpslAttribute("*rm", "THE QUICK BROWN FOX.")), is(true));
    }

    @Test
    public void validateSyntax_syntax_error_and_invalid_email() {
        final ObjectMessages objectMessages = new ObjectMessages();
        final RpslAttribute rpslAttribute = new RpslAttribute("inetnum", "auto-dbm@ripe.net");
        rpslAttribute.validateSyntax(ObjectType.INETNUM, objectMessages);

        assertThat(objectMessages.getMessages(rpslAttribute).getAllMessages(), contains(ValidationMessages.syntaxError("auto-dbm@ripe.net")));
    }

    @Test
    public void format_single_line_no_spaces() {
        final RpslAttribute subject = new RpslAttribute("person", "Brian Riddle");
        assertThat(subject.toString(), is("person:         Brian Riddle\n"));
    }

    @Test
    public void format_single_line_some_spaces() {
        final RpslAttribute subject = new RpslAttribute("person", "    Brian Riddle");
        assertThat(subject.toString(), is("person:         Brian Riddle\n"));
    }

    @Test
    public void format_single_line_too_many_spaces() {
        final RpslAttribute subject = new RpslAttribute("person", "       Brian Riddle");
        assertThat(subject.toString(), is("person:         Brian Riddle\n"));
    }

    @Test
    public void reference_value_reference() {
        subject = new RpslAttribute("mnt-by", "DEV-MNT");
        assertThat(subject.getReferenceValue().toString(), is("DEV-MNT"));
    }

    @Test
    public void reference_value_not_reference() {
        subject = new RpslAttribute("descr", "Some description");
        assertThat(subject.getReferenceValue().toString(), is("Some description"));
    }

    @Test
    public void reference_value_mnt_routes() {
        subject = new RpslAttribute("mnt-routes", "DEV-MNT ANY");
        assertThat(subject.getReferenceValue().toString(), is("DEV-MNT"));
    }

    @Test
    public void get_comment_in_second_line() {
        subject = new RpslAttribute("remarks", "remark1\n remark2 # comment");
        assertThat(subject.getCleanComment(), is("comment"));

        subject = new RpslAttribute("remarks", "foo\t  # comment1 \n bar # \t comment2\n+ bla");
        assertThat(subject.getCleanComment(), is("comment1 comment2"));
    }

    @Test
    public void preserve_and_allign_multiline_attributes() {
        //[TP] we do not support leading spaces in values for formatting.
        //all attribute value lines are automatically aligned to RpslAttribute.LEADING_CHARS(_SHORTHAND)
        subject = new RpslAttribute("remarks",
                " start\n" +
                "         line\n" +
                "            line");

        assertThat(subject.getValue(), is(
                " start\n" +
                "         line\n" +
                "            line"));

        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "                line\n" +
                "                line"));

        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "                line\n" +
                "                line\n"));
    }

    @Test
    public void single_space_continuation_character() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                " \n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                " \n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n" +
                "                line\n\n"));
    }

    @Test
    public void single_plus_continuation_character() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "+\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "+\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n" +
                "                line\n\n"));
     }

    @Test
    public void single_tab_continuation_character() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "\t\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "\t\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n" +
                "                line\n\n"));
    }

    @Test
    public void single_space_continuation_character_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                " line\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                " line\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "                line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "                line\n" +
                "                line\n\n"));
    }

    @Test
    public void single_plus_continuation_character_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "+line\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "+line\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+               line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+               line\n" +
                "                line\n\n"));
    }

    @Test
    public void single_tab_continuation_character_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "\tline\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "\tline\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "                line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "                line\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_space_continuation_characters() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "   \n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "   \n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_plus_continuation_characters() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "+++\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "+++\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+               ++\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+               ++\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_tab_continuation_characters() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "\t\t\t\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "\t\t\t\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_space_continuation_characters_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "   line\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "   line\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "                line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "                line\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_plus_continuation_characters_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "+++line\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "+++line\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+               ++line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+               ++line\n" +
                "                line\n\n"));
    }

    @Test
    public void multiple_tab_continuation_characters_and_value() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "\t\t\tline\n" +
                "line\n");

        assertThat(subject.getValue(), is(
                " start\n" +
                "\t\t\tline\n" +
                "line\n"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "                line\n" +
                "                line\n"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "                line\n" +
                "                line\n\n"));
    }

    @Test
    public void single_space_continuation_character_at_end_of_attribute() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                " ");

        assertThat(subject.getValue(), is(
                " start\n" +
                " "));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n"));
    }

    @Test
    public void single_plus_continuation_character_at_end_of_attribute() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "+");

        assertThat(subject.getValue(), is(
                " start\n" +
                "+"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n"));
    }

    @Test
    public void single_tab_continuation_character_at_end_of_attribute() {
        subject = new RpslAttribute("remarks",
                " start\n" +
                "\t");

        assertThat(subject.getValue(), is(
                " start\n" +
                "\t"));
        assertThat(subject.getFormattedValue(), is(
                "        start\n" +
                "+"));
        assertThat(subject.toString(), is(
                "remarks:        start\n" +
                "+\n"));
    }

    @Test
    public void empty_attribute_value() {
        subject = new RpslAttribute("remarks", "");

        assertThat(subject.getValue(), is(""));
        assertThat(subject.getFormattedValue(), is(""));
        assertThat(subject.toString(), is("remarks:\n"));
    }

    @Test
    public void spaces_only_in_attribute_value() {
        subject = new RpslAttribute("remarks", "   ");

        assertThat(subject.getValue(), is("   "));
        assertThat(subject.getFormattedValue(), is(""));
        assertThat(subject.toString(), is("remarks:\n"));
    }
}