package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.support.AbstractDaoTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Scanner;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ResourceDataDaoTest extends AbstractDaoTest {
    @Autowired ResourceDataDao subject;
    Logger logger = LoggerFactory.getLogger(ResourceDataDaoTest.class);

    @Test
    public void test_store_load_cycle() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "TEST-GRS", new Scanner("" +
                "test|EU|asn|7|1|19930901|allocated\n" +
                "test|EU|asn|28|10|19930901|allocated\n" +
                "test|FR|ipv4|2.0.0.0|1048576|20100712|allocated\n" +
                "test|EU|ipv4|2.16.0.0|524288|20100910|allocated\n" +
                "test|DE|ipv6|2001:608::|32|19990812|allocated\n" +
                "test|NL|ipv6|2001:610::|32|19990819|allocated\n"));

        assertThat(resourceData.getResources(), hasSize(15));

        subject.store("test", resourceData);
        final AuthoritativeResource loadedData = subject.load(logger, "test");
        assertEquals(resourceData, loadedData);
    }

    @Test
    public void load_nonexistent_source() {
        final AuthoritativeResource loadedData = subject.load(logger, "zoh");
        assertThat(loadedData.getResources(), hasSize(0));
    }
}
