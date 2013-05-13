package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RpslAttributeTest {
    private RpslAttribute subject;

    @Test
    public void remove_comments_single_line() throws Exception {
        subject = new RpslAttribute("source", "    RIPE #");
        assertThat(subject.getCleanValue().toString(), is("RIPE"));
    }

    @Test
    public void remove_comments_on_single_line() throws Exception {
        subject = new RpslAttribute("source", "    RIPE # Some comment");
        assertThat(subject.getCleanValue().toString(), is("RIPE"));
    }

    @Test
    public void remove_comments_multiple_lines() throws Exception {
        subject = new RpslAttribute("source", "    RIPE #\n RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE RIPE"));
    }

    @Test
    public void remove_comments_multiple_lines_with_continuation() throws Exception {
        subject = new RpslAttribute("source", "    RIPE #\n+ RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE RIPE"));
    }

    @Test
    public void remove_comments_with_plus() throws Exception {
        subject = new RpslAttribute("source", "    RIPE + RIPE");
        assertThat(subject.getCleanValue().toString(), is("RIPE + RIPE"));
    }

    @Test
    public void remove_comments_with_plusses() throws Exception {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa"));
    }

    @Test
    public void remove_comments_with_plusses_and_data() throws Exception {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+     asd");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa asd"));
    }

    @Test
    public void remove_comments_with_plusses_and_plus() throws Exception {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+++     asd");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa ++ asd"));
    }

    @Test
    public void remove_comments_with_plusses_and() throws Exception {
        subject = new RpslAttribute("domain", "186.35.194.in-addr.arpa\n+\n+\n+asd\n");
        assertThat(subject.getCleanValue().toString(), is("186.35.194.in-addr.arpa asd"));
    }

    @Test
    public void remove_comments_with_spaces() throws Exception {
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
    public void validateSyntax_syntax_valid_and_auto_dbm() {
        final ObjectMessages objectMessages = new ObjectMessages();
        final RpslAttribute rpslAttribute = new RpslAttribute("remarks", "auto-dbm@ripe.net");
        rpslAttribute.validateSyntax(ObjectType.DOMAIN, objectMessages);

        assertThat(objectMessages.getMessages(rpslAttribute).getAllMessages(), contains(ValidationMessages.emailAddressNotAllowed("auto-dbm@ripe.net")));
    }

    @Test
    public void validateSyntax_syntax_valid_and_test_dbm() {
        final ObjectMessages objectMessages = new ObjectMessages();
        final RpslAttribute rpslAttribute = new RpslAttribute("remarks", "TEST-dbm@ripe.net");
        rpslAttribute.validateSyntax(ObjectType.DOMAIN, objectMessages);

        assertThat(objectMessages.getMessages(rpslAttribute).getAllMessages(), contains(ValidationMessages.emailAddressNotAllowed("TEST-dbm@ripe.net")));
    }

    @Test
    public void validateSyntax_syntax_valid_and_invalid_email_contains() {
        final ObjectMessages objectMessages = new ObjectMessages();
        final RpslAttribute rpslAttribute = new RpslAttribute("remarks", "Some remark about auto-dbm@RIPE.net should be detected");
        rpslAttribute.validateSyntax(ObjectType.DOMAIN, objectMessages);

        assertThat(objectMessages.getMessages(rpslAttribute).getAllMessages(), contains(ValidationMessages.emailAddressNotAllowed("auto-dbm@RIPE.net")));
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
        final RpslAttribute subject = new RpslAttribute("person", "                       Brian Riddle");
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
        subject = new RpslAttribute("mnt-routes", "DEV-MNT {ANY}");
        assertThat(subject.getReferenceValue().toString(), is("DEV-MNT"));
    }
}
