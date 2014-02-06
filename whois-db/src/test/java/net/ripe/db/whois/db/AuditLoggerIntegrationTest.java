package net.ripe.db.whois.db;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.FileHelper;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-whois-test.xml"})
public class AuditLoggerIntegrationTest extends AbstractIntegrationTest {

    @Value("${dir.update.audit.log}")
    String auditLog;

    @Autowired
    WhoisObjectClientMapper whoisObjectMapper;

    @Before
    public void setup() {
        databaseHelper.addObject(RpslObject.parse("" +
                "person:    Test Person\n" +
                "nic-hdl:   TP1-TEST\n" +
                "source:    TEST"));
        databaseHelper.addObject(RpslObject.parse("" +
                "mntner:    TST-MNT\n" +
                "descr:     description\n" +
                "admin-c:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "upd-to:    dbtest@ripe.net\n" +
                "auth:      MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed:   dbtest@ripe.net 20120707\n" +
                "source:    TEST\n"));
        databaseHelper.updateObject(RpslObject.parse("" +
                "person:    Test Person\n" +
                "address:   NL\n" +
                "phone:     +31 20 123456\n" +
                "nic-hdl:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   dbtest@ripe.net 20120101\n" +
                "source:    TEST\n"));
    }

    @Test
    public void rest_update_gets_logged() throws Exception {
        testDateTimeProvider.setTime(LocalDateTime.parse("2001-02-03T17:00:00"));
        RpslObject rpslObject = RpslObject.parse("" +
                "person:    Test Person\n" +
                "address:   NL\n" +
                "phone:     +31 20 123456\n" +
                "nic-hdl:   TP1-TEST\n" +
                "mnt-by:    TST-MNT\n" +
                "changed:   dbtest@ripe.net 20120102\n" +
                "source:    TEST\n");

        final String result = RestTest.target(getPort(), "whois/test/person/TP1-TEST?password=update")
                .request(MediaType.APPLICATION_XML)
                .put(Entity.entity(whoisObjectMapper.mapRpslObjects(Arrays.asList(rpslObject)), MediaType.APPLICATION_XML), String.class);

        System.err.println(result);

        String audit = FileHelper.fetchGzip(new File(auditLog + "/20010203/170000.rest_127.0.0.1_0/000.audit.xml.gz"));
        assertThat(audit, containsString("<query"));
        assertThat(audit, containsString("<sql"));
        // TODO: test thoroughly once the test is not broken anymore
    }
}
