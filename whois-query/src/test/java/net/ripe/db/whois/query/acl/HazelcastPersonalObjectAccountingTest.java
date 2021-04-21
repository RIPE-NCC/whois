package net.ripe.db.whois.query.acl;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.Inet4Address;
import java.net.InetAddress;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastPersonalObjectAccountingTest {
    private InetAddress ipv4Address;

    private static HazelcastPersonalObjectAccounting subject;
    private static HazelcastInstance instance;

    @BeforeClass
    public static void startHazelcast() {
       instance = Hazelcast.newHazelcastInstance(null);
       subject = new HazelcastPersonalObjectAccounting(instance);
    }

    @AfterClass
    public static void shutdownHazelcast() {
        instance.getLifecycleService().shutdown();
    }

    @Before
    public void setUp() throws Exception {
        subject.resetAccounting();

        ipv4Address = Inet4Address.getLocalHost();
    }

    @Test
    public void test_queried_personal_objects() {
        assertThat(subject.getQueriedPersonalObjects(ipv4Address), is(0));
    }

    private void test_account_personal_object(int amount) {
        final int balance = subject.accountPersonalObject(ipv4Address, amount);
        assertThat(balance, is(amount));
        assertThat(subject.getQueriedPersonalObjects(ipv4Address), is(amount));
    }

    @Test
    public void test_account_personal_object_amount_1() {
        for (int i = 1; i < 100; i++) {
            int balance = subject.accountPersonalObject(ipv4Address, 1);
            assertThat(balance, is(i));
        }
    }

    @Test
    public void test_account_personal_object_amount_5() {
        test_account_personal_object(5);
    }

    @Test
    public void test_account_personal_object_amount_personal_limit() {
        test_account_personal_object(25000);
    }

    @Test
    public void test_reset_personal_object_limits() {
        subject.accountPersonalObject(ipv4Address, 1);
        assertThat(subject.getQueriedPersonalObjects(ipv4Address), is(1));

        subject.resetAccounting();

        assertThat(subject.getQueriedPersonalObjects(ipv4Address), is(0));
    }
}
