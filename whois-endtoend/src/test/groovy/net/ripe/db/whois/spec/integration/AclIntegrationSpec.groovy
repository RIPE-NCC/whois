package net.ripe.db.whois.spec.integration
import com.google.common.collect.Maps
import net.ripe.db.whois.common.IntegrationTest
import org.junit.Before
import spock.lang.Ignore

@Ignore
@org.junit.experimental.categories.Category(IntegrationTest.class)
class AclIntegrationSpec extends BaseWhoisSourceSpec {

    @Before
    def "setup"() {
        getDatabaseHelper().insertApiKey("DB-WHOIS-testapikey", "/api/acl/bans", "acl api key");
    }

    @Override
    Map<String, String> getFixtures() {
        return Maps.newHashMap()
    }

    def "ban with invalid date doesn't return stack trace"() {
      when:
        def response = saveAcl(
                "bans",
                "DB-WHOIS-testapikey",
                """
                    {
                      "prefix" : "10.1.2.1/32",
                      "comment" : "Just Testing",
                      "since" : "invalid"
                    }
                """.stripIndent(),
                HttpURLConnection.HTTP_BAD_REQUEST)
      then:
        !(response =~ /Caused by:/)
        response =~ /Can not construct instance of java.util.Date from String value 'invalid': not a valid representation/
    }

    def "ban with invalid IPv4 prefix length"() {
      when:
        def response = saveAcl(
                "bans",
                "DB-WHOIS-testapikey",
                """
                    {
                      "prefix" : "10.1.2.0/24",
                      "comment" : "comment",
                      "since" : "2013-01-01"
                    }
                """.stripIndent(),
                HttpURLConnection.HTTP_BAD_REQUEST)
      then:
        response =~ /IPv4 must be a single address/
    }
}
