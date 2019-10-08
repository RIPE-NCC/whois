package net.ripe.db.whois.common.conversion;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class PasswordFilterTest {

    @Test
    public void testRobustness() {
        assertThat(PasswordFilter.filterPasswordsInContents(null), is(nullValue()));
        assertThat(PasswordFilter.filterPasswordsInUrl(null), is(nullValue()));
    }

    @Test
    public void testFilterPasswordsInMessage() {
        final String input =
                "red: adsfasdf\n" +
                "blue: asdfasdfasdf\n" +
                "yellow%3A++asdfasdfasdf\n" +
                "green: asdfasdfasdf # password: test\n" +
                "purple: password\n" +
                "password:   test1 \n" +
                "password:test2\n" +
                "password: test3\n" +
                "password%3A++test4\n" +
                "password%3A++test5\n" +
                "delete: adsf\n";

        assertThat(PasswordFilter.filterPasswordsInContents(input), containsString(
                "red: adsfasdf\n" +
                "blue: asdfasdfasdf\n" +
                "yellow%3A++asdfasdfasdf\n" +
                "green: asdfasdfasdf # password: test\n" +
                "purple: password\n" +
                "password:FILTERED\n" +
                "password:FILTERED\n" +
                "password:FILTERED\n" +
                "password%3AFILTERED\n" +
                "password%3AFILTERED\n" +
                "delete: adsf\n"));
    }

    @Test
    public void testFilterOverridePasswordsInMessage() {
        final String input =
                "red: adsfasdf\n" +
                "blue: asdfasdfasdf\n" +
                "yellow%3A++asdfasdfasdf\n" +
                "green: asdfasdfasdf # override: test\n" +
                "purple: override\n" +
                "override:user,pass\n" +
                "override:user,pass,reason\n" +
                "override:   user,pass\n" +
                "override%3A++user,pass\n" +
                "delete: adsf\n";

        assertThat(PasswordFilter.filterPasswordsInContents(input), containsString(
                "red: adsfasdf\n" +
                "blue: asdfasdfasdf\n" +
                "yellow%3A++asdfasdfasdf\n" +
                "green: asdfasdfasdf # override: test\n" +
                "purple: override\n" +
                "override:user,FILTERED\n" +
                "override:user,FILTERED,reason\n" +
                "override:user,FILTERED\n" +
                "override%3A++user,FILTERED\n" +
                "delete: adsf\n"));
    }

    @Test
    public void testFilterOverrideAndPasswordsInMessage() {
        final String input =
                "red: adsfasdf\n" +
                "purple: override\n" +
                "override:user,pass\n" +
                "password:test\n";

        assertThat(PasswordFilter.filterPasswordsInContents(input), containsString(
                "red: adsfasdf\n" +
                "purple: override\n" +
                "override:user,FILTERED\n" +
                "password:FILTERED")); // eol stripped
     }


    @Test
    public void password_filtering_in_url() {

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=secret"),
                                                    is("/some/path?password=FILTERED"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=p%3Fssword%26"),
                                                    is("/some/path?password=FILTERED"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=secret&param"),
                                                    is("/some/path?password=FILTERED&param"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=secret&password=other"),
                                                    is("/some/path?password=FILTERED&password=FILTERED"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=secret&password=other&param=value"),
                                                    is("/some/path?password=FILTERED&password=FILTERED&param=value"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?param=value&password=secret&password=other"),
                                                    is("/some/path?param=value&password=FILTERED&password=FILTERED"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?param=value&password=secret&param=password"),
                                                    is("/some/path?param=value&password=FILTERED&param=password"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?password=test$#@!%^*-ab&param=other"),
                                                    is("/some/path?password=FILTERED&param=other"));

        assertThat( PasswordFilter.filterPasswordsInUrl("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0apassword%3a+team-red%0a&NEW=yes"),
                                                     is("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0apassword%3aFILTERED&NEW=yes"));

        //TODO [TP] : lines after the password are cut off
        assertThat( PasswordFilter.filterPasswordsInUrl("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0apassword%3a+team-red%0a%0anotify%3a+email%40ripe.net%0a&NEW=yes"),
                                                     is("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0apassword%3aFILTERED&NEW=yes"));
    }

    @Test
    public void testFilterWithOverrideInUrl() {
        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?override=admin,secret&param=other"),
                                                    is("/some/path?override=admin,FILTERED&param=other"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?override=admin,secret,reason&param=other"),
                                                    is("/some/path?override=admin,FILTERED,reason&param=other"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?DATA=person:++Test%2BPerson%0Asource:++TEST%0A%0Aoverride:admin,password&NEW=yes"),
                                                    is("/some/path?DATA=person:++Test%2BPerson%0Asource:++TEST%0A%0Aoverride:admin,FILTERED&NEW=yes"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?DATA=person:++Test%2BPerson%0Asource:++TEST%0A%0Aoverride:admin,password,reason&NEW=yes"),
                                                    is("/some/path?DATA=person:++Test%2BPerson%0Asource:++TEST%0A%0Aoverride:admin,FILTERED,reason&NEW=yes"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?DATA=person:++TestP%0A%0Aoverride:personadmin,team-red1234&NEW=yes"),
                                                    is("/some/path?DATA=person:++TestP%0A%0Aoverride:personadmin,FILTERED&NEW=yes"));

        assertThat(PasswordFilter.filterPasswordsInUrl("/some/path?DATA=person:++TestP%0A%0Aoverride%3Apersonadmin,team-red1234&NEW=yes"),
                                                    is("/some/path?DATA=person:++TestP%0A%0Aoverride%3Apersonadmin,FILTERED&NEW=yes"));

        assertThat(PasswordFilter.filterPasswordsInUrl(
                "whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0Aaddress%3A" +
                "++++++++Singel+258%0Aphone%3A++++++++++%2B31+6+12345678%0Anic-hdl%3A" +
                "++++++++TP2-TEST%0Amnt-by%3A+++++++++OWNER-MNT%0Achanged%3A" +
                "++++++++dbtest%40ripe.net+20120101%0Asource%3A+++++++++TEST%0A" +
                "override%3Apersonadmin%2Cteam-red1234&NEW=yes"),
             is("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0Aaddress%3A" +
                "++++++++Singel+258%0Aphone%3A++++++++++%2B31+6+12345678%0Anic-hdl%3A" +
                "++++++++TP2-TEST%0Amnt-by%3A+++++++++OWNER-MNT%0Achanged%3A" +
                "++++++++dbtest%40ripe.net+20120101%0Asource%3A+++++++++TEST%0A" +
                "override%3Apersonadmin,FILTERED&NEW=yes"));

        assertThat(PasswordFilter.filterPasswordsInUrl(
                   "whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0Aoverride:+admin,teamred,reason%0anotify%3a+email%40ripe.net%0a&NEW=yes"),
                is("whois/syncupdates/test?DATA=person%3A+++++++++Test+Person%0asource%3a+RIPE%0Aoverride:+admin,FILTERED,reason%0anotify%3a+email%40ripe.net%0a&NEW=yes"));
    }
}
