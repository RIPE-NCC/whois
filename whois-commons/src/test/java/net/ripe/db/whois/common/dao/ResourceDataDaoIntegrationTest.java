package net.ripe.db.whois.common.dao;


import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.support.AbstractDaoIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("IntegrationTest")
public class ResourceDataDaoIntegrationTest extends AbstractDaoIntegrationTest {
    @Autowired @Qualifier("internalsDataSource")
    DataSource dataSource;
    @Autowired ResourceDataDao subject;
    Logger logger = LoggerFactory.getLogger(ResourceDataDaoIntegrationTest.class);

    @Test
    public void test_store_load_cycle() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "TEST-GRS", new Scanner("" +
                "test|EU|asn|7|1|19930901|allocated\n" +
                "test|EU|asn|28|10|19930901|allocated\n" +
                "test|FR|ipv4|10.2.0.0|1024|20100712|allocated\n" +
                "test|EU|ipv4|10.16.0.0|2048|20100910|allocated\n" +
                "test|DE|ipv6|2001:608::|32|19990812|allocated\n" +
                "test|NL|ipv6|2001:610::|64|19990819|allocated\n"));

        assertThat(resourceData.getResources(), hasSize(6));

        subject.store("test", resourceData);
        final AuthoritativeResource loadedData = subject.load("test");
        assertThat(resourceData, equalTo(loadedData));
    }

    @Test
    public void support_old_format() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("insert into authoritative_resource (source, resource) VALUES ('ripe', 'AS1')");
        jdbcTemplate.execute("insert into authoritative_resource (source, resource) VALUES ('ripe', 'AS2 - AS2')");
        jdbcTemplate.execute("insert into authoritative_resource (source, resource) VALUES ('ripe', 'AS3 - AS5')");
        final AuthoritativeResource authoritativeResource = subject.load("ripe");

        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS1")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS2")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS3")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS4")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS5")), is(true));
        assertThat(authoritativeResource.isMaintainedInRirSpace(ObjectType.AUT_NUM, CIString.ciString("AS6")), is(false));
    }

    @Test
    public void load_nonexistent_source() {
        final AuthoritativeResource loadedData = subject.load("zoh");
        assertThat(loadedData.getResources(), hasSize(0));
    }

    @Test
    public void compare_state() {
        assertThat(new ResourceDataDao.State("test", 1, 1).compareTo(new ResourceDataDao.State("test", 0, 0)), is(1));
        assertThat(new ResourceDataDao.State("test", 1, 1).compareTo(new ResourceDataDao.State("test", 1, 1)), is(0));
        assertThat(new ResourceDataDao.State("test", 1, 1).compareTo(new ResourceDataDao.State("test", 2, 2)), is(-1));
    }
}
