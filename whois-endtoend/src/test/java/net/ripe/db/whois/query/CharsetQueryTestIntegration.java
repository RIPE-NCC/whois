package net.ripe.db.whois.query;

import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public class CharsetQueryTestIntegration extends AbstractQueryIntegrationTest {

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     PP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "auth:        PGPKEY-A8D16B70\n" +
            "mnt-by:      OWNER-MNT\n" +
            "source:      TEST");

    @BeforeEach
    public void startupWhoisServer() {
        databaseHelper.addObjects(PAULETH_PALTHEN, OWNER_MNT);

        queryServer.start();
    }

    @Test
    public void inverse_email_charset() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER1-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     PP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "auth:        PGPKEY-A8D16B70\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final String rpslObject = "" +
                "person:         Pauleth Palthen\n" +
                "address:        Singel 258\n" +
                "phone:          +31-1234567890\n" +
                "remarks:        é, Ú, ß\n" +
                "mnt-by:         OWNER1-MNT\n" +
                "nic-hdl:        PP2-TEST\n" +
                "source:         TEST";

        databaseHelper.addObject(rpslObject);

        final Pattern pattern = Pattern.compile("remarks:\s+(.*?)\s*\n");

        /* Default encoding */
        final String defaultEncodingResponse = query("-Bi mnt-by OWNER1-MNT");
        Matcher matcher = pattern.matcher(defaultEncodingResponse);
        assertThat(matcher.find(), is(true));
        assertThat("é, Ú, ß", is(matcher.group(1)));

        /* Latin-1 encoding */
        final String latin1Response = query("-Z latin-1 -Bi mnt-by OWNER1-MNT");
        matcher = pattern.matcher(latin1Response);
        assertThat(matcher.find(), is(true));
        assertThat("é, Ú, ß", is(matcher.group(1)));

        /* UTF-8 encoding */
        final String utf8Response = query("-Z utf8 -Bi mnt-by OWNER1-MNT", StandardCharsets.UTF_8);
        matcher = pattern.matcher(utf8Response);
        assertThat(matcher.find(), is(true));
        assertThat("é, Ú, ß", is(matcher.group(1)));
    }

    @Test
    public void inverse_email_java_recognised_charset() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER1-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     PP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "auth:        PGPKEY-A8D16B70\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final String rpslObject = "" +
                "person:         Pauleth Palthen\n" +
                "address:        Singel 258\n" +
                "phone:          +31-1234567890\n" +
                "remarks:        é, Ú, ß\n" +
                "mnt-by:         OWNER1-MNT\n" +
                "nic-hdl:        PP2-TEST\n" +
                "source:         TEST";

        databaseHelper.addObject(rpslObject);

        final Pattern pattern = Pattern.compile("remarks:\s+(.*?)\s*\n");

        final String usAsciiEncodingResponse = query("-Z US-ASCII -Bi mnt-by OWNER1-MNT", StandardCharsets.US_ASCII);
        Matcher matcher = pattern.matcher(usAsciiEncodingResponse);
        assertThat(matcher.find(), is(true));
        assertThat("?, ?, ?", is(matcher.group(1)));
    }

    @Test
    public void inverse_email_latin2_charset() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER1-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     PP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "auth:        PGPKEY-A8D16B70\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final String rpslObject = "" +
                "person:         Pauleth Palthen\n" +
                "address:        Singel 258\n" +
                "phone:          +31-1234567890\n" +
                "remarks:        é, Ú, ß\n" +
                "mnt-by:         OWNER1-MNT\n" +
                "nic-hdl:        PP2-TEST\n" +
                "source:         TEST";

        databaseHelper.addObject(rpslObject);

        final Pattern pattern = Pattern.compile("remarks:\s+(.*?)\s*\n");

        final String latin2EncodingResponse = query("-Z ISO-8859-2 -Bi mnt-by OWNER1-MNT", Charset.forName("ISO-8859-2"));
        Matcher matcher = pattern.matcher(latin2EncodingResponse);
        assertThat(matcher.find(), is(true));
        assertThat("é, Ú, ß", is(matcher.group(1)));
    }

    @Test
    public void inverse_email_cyrillic_charset() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER1-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     PP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "auth:        PGPKEY-A8D16B70\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final String rpslObject = "" +
                "person:         Pauleth Palthen\n" +
                "address:        Singel 258\n" +
                "phone:          +31-1234567890\n" +
                "remarks:        A, B, Ѣ, H, O, Ꙋ\n" +
                "mnt-by:         OWNER1-MNT\n" +
                "nic-hdl:        PP2-TEST\n" +
                "source:         TEST";

        databaseHelper.addObject(rpslObject);

        final Pattern pattern = Pattern.compile("remarks:\s+(.*?)\s*\n");

        final String cyrillicEncodingResponse = query("-Z CP1251 -Bi mnt-by OWNER1-MNT", Charset.forName("CP1251"));
        Matcher matcher = pattern.matcher(cyrillicEncodingResponse);
        assertThat(matcher.find(), is(true));
        assertThat("A, B, ?, H, O, ?", is(matcher.group(1)));
    }

    @Test
    public void inverse_email_non_recognised_charset() {
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:      OWNER1-MNT\n" +
                "descr:       Owner Maintainer\n" +
                "admin-c:     PP1-TEST\n" +
                "upd-to:      noreply@ripe.net\n" +
                "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "auth:        SSO person@net.net\n" +
                "auth:        PGPKEY-A8D16B70\n" +
                "mnt-by:      OWNER-MNT\n" +
                "source:      TEST"));

        final String rpslObject = "" +
                "person:         Pauleth Palthen\n" +
                "address:        Singel 258\n" +
                "phone:          +31-1234567890\n" +
                "remarks:        é, Ú, ß\n" +
                "mnt-by:         OWNER1-MNT\n" +
                "nic-hdl:        PP2-TEST\n" +
                "remarks:        remark\n" +
                "source:         TEST";

        databaseHelper.addObject(rpslObject);

        assertThat(query("-Z nonExistCharset -Bi mnt-by OWNER1-MNT"), containsString("Invalid character set nonExistCharset"));
    }

    @Test
    public void non_ascii_query() {
        databaseHelper.addObject(
                "person:    Tëst Pärsön\n" +
                        "address:   Singel 258\n" +
                        "phone:     +31-1234567890\n" +
                        "e-mail:    noreply@ripe.net\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "nic-hdl:   TP1-TEST\n" +
                        "remarks:   remark\n" +
                        "source:    TEST\n");

        final String response = query("-Z utf8 Pärsön", StandardCharsets.UTF_8);

        assertThat(response, containsString("Tëst Pärsön"));
    }

    @Test
    public void multiple_charset_then_correct_error() {
        databaseHelper.addObject(
                "person:    Tëst Pärsön\n" +
                        "address:   Singel 258\n" +
                        "phone:     +31-1234567890\n" +
                        "e-mail:    noreply@ripe.net\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "nic-hdl:   TP1-TEST\n" +
                        "remarks:   remark\n" +
                        "source:    TEST\n");

        final String response = query("-Z utf8 -Z latin1 Pärsön", StandardCharsets.UTF_8);

        assertThat(response, containsString("""
                %ERROR:110: multiple use of flag
                %
                % The flag "-Z" cannot be used multiple times."""));
    }
}
