package net.ripe.db.whois.common;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PunycodeConversionTest {

    @Test
    public void convert_email() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_trailing_spaces() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@zürich.example   \n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--zrich-kva.example   \n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_trailing_spaces_with_comment() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@zürich.example   # TODO \n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--zrich-kva.example   # TODO \n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_many_trailing_spaces() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@zürich.example" + " ".repeat(100) + "\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--zrich-kva.example" + " ".repeat(100) + "\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_many_trailing_spaces_with_comment() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@zürich.example" + " ".repeat(100)+ "# TODO \n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--zrich-kva.example" + " ".repeat(100)+ "# TODO \n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_local_part_only() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_double_at_address() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--@zrich-4ya.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_dots() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@...zürich..example...\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@...zürich..example...\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_name_and_address() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: Example Usër <example@zürich.example>\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: Example Usër <example@xn--zrich-kva.example>\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_name_and_address_and_many_spaces_and_comment() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:" + " ".repeat(100) + "Example Test Usër <example@zürich.example>" + " ".repeat(100) + "# comment   \n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:" + " ".repeat(100) + "Example Test Usër <example@xn--zrich-kva.example>" + " ".repeat(100) + "# comment   \n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_domain_only_not_local_part() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-rëply@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-rëply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_cyrillic() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@москва.ru\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail: no-reply@xn--80adxhks.ru\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_with_tabs() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:\t\tno-reply@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:\t\tno-reply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_email_with_tabs_and_spaces() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:\t \t  no-reply@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   TR1-TEST\n" +
                "e-mail:\t \t  no-reply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_multiple_email() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "e-mail:    user@zürich.example\n" +
                "e-mail:    no-reply@zürich.example\n" +
                "source:    TEST\n";

       final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "e-mail:    user@xn--zrich-kva.example\n" +
                "e-mail:    no-reply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_multiple_trailing_email() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "source:    TEST\n" +
                "e-mail:    user@zürich.example\n" +
                "e-mail:    no-reply@zürich.example";

       final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "source:    TEST\n" +
                "e-mail:    user@xn--zrich-kva.example\n" +
                "e-mail:    no-reply@xn--zrich-kva.example"));
    }

    @Test
    public void convert_multiple_email_with_separator_remark() {
        final String value =
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "e-mail:    user@zürich.example\n" +
                "remarks:   separator\n" +
                "e-mail:    no-reply@zürich.example\n" +
                "source:    TEST\n";

       final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Test Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "e-mail:    user@xn--zrich-kva.example\n" +
                "remarks:   separator\n" +
                "e-mail:    no-reply@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_abuse_mailbox() {
        final String value =
                "role:     Abuse Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "abuse-mailbox: abuse@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "role:     Abuse Role\n" +
                "nic-hdl:   AR1-TEST\n" +
                "abuse-mailbox: abuse@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_irt_nfy() {
        final String value =
                "irt:     IRT-EXAMPLE\n" +
                "irt-nfy: irt@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "irt:     IRT-EXAMPLE\n" +
                "irt-nfy: irt@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_mnt_nfy() {
        final String value =
                "mntner:     EXAMPLE-MNT\n" +
                "mnt-nfy: notify@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "mntner:     EXAMPLE-MNT\n" +
                "mnt-nfy: notify@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_notify() {
        final String value =
                "mntner:     EXAMPLE-MNT\n" +
                "notify: notify@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "mntner:     EXAMPLE-MNT\n" +
                "notify: notify@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_ref_nfy() {
        final String value =
                "organisation:     ORG-EX1-TEST\n" +
                "ref-nfy: notify@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "organisation:     ORG-EX1-TEST\n" +
                "ref-nfy: notify@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }

    @Test
    public void convert_upd_to() {
        final String value =
                "mntner:     EXAMPLE-MNT\n" +
                "upd-to: notify@zürich.example\n" +
                "source:    TEST\n";

        final String result = PunycodeConversion.convert(value);

        assertThat(result, is(
                "mntner:     EXAMPLE-MNT\n" +
                "upd-to: notify@xn--zrich-kva.example\n" +
                "source:    TEST\n"));
    }
}
