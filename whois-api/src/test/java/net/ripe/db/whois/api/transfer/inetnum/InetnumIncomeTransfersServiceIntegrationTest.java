package net.ripe.db.whois.api.transfer.inetnum;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceDao;
import net.ripe.db.whois.api.transfer.logic.inetnum.InetnumTransfer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@Category(IntegrationTest.class)
public class InetnumIncomeTransfersServiceIntegrationTest extends AbstractInetnumTransferInternalTest {

    private static final String INETNUM_NON_RIPE_202_8 = "" +
            "inetnum:        202.0.0.0 - 202.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_203_8 = "" +
            "inetnum:        203.0.0.0 - 203.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_204_8 = "" +
            "inetnum:        204.0.0.0 - 204.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_205_8 = "" +
            "inetnum:        205.0.0.0 - 205.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_206_8 = "" +
            "inetnum:        206.0.0.0 - 206.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_207_8 = "" +
            "inetnum:        207.0.0.0 - 207.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_201_8 = "" +
            "inetnum:        201.0.0.0 - 201.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_201_0_16 = "" +
            "inetnum:        201.0.0.0 - 201.0.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_201_0_0_24 = "" +
            "inetnum:        201.0.0.0 - 201.0.0.255\n" +
            "netname:        Some netname\n" +
            "descr:          IPv4 address block for test only\n" +
            "country:        PT\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED PI\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_193_0_0_24 = "" +
            "inetnum:        193.0.0.0 - 193.0.0.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_RIPE_193_0_16 = "" +
            "inetnum:        193.0.0.0 - 193.0.255.255\n" +
            "netname:         EU-ZZ-200-0\n" +
            "descr:           To determine the registration information for a more\n" +
            "descr:           specific range, please try a more specific query.\n" +
            "descr:           If you see this object as a result of a single IP query,\n" +
            "descr:           it means the IP address is currently in the free pool of\n" +
            "descr:           address space managed by the RIPE NCC.\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_RIPE_193_8 = "" +
            "inetnum:        193.0.0.0 - 193.255.255.255\n" +
            "netname:         EU-ZZ-200\n" +
            "descr:           To determine the registration information for a more\n" +
            "descr:           specific range, please try a more specific query.\n" +
            "descr:           If you see this object as a result of a single IP query,\n" +
            "descr:           it means the IP address is currently in the free pool of\n" +
            "descr:           address space managed by the RIPE NCC.\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String DOMAIN_UNDER_200_0_16_X = "" +
            "domain:          6.0.200.in-addr.arpa\n" +
            "descr:           RIPE NCC Internal Use\n" +
            "admin-c:         PERSON-TEST\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String DOMAIN_UNDER_200_0_16_Y = "" +
            "domain:          7.0.200.in-addr.arpa\n" +
            "descr:           RIPE NCC Internal Use\n" +
            "admin-c:         PERSON-TEST\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_NON_RIPE_UNDER_200_0_16_X = "" +
            "inetnum:        200.0.128.0 - 200.0.128.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_UNDER_200_0_16_Y = "" +
            "inetnum:        200.0.128.129 - 200.0.128.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String ROUTE_UNDER_200_0_16_X = "" +
            "route:           200.0.0.0/17\n" +
            "descr:           Some Route\n" +
            "origin:          AS12345666\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String ROUTE_UNDER_200_0_16_Y = "" +
            "route:           200.0.0.0/18\n" +
            "descr:           Some Route\n" +
            "origin:          AS66654321\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_NON_RIPE_200_8 = "" +
            "inetnum:        200.0.0.0 - 200.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    @Autowired
    private AuthoritativeResourceDao authoritativeResourceDao;

    @Before
    public void setUpEach() {
        authoritativeResourceDao.delete("test", "0.0.0.0/0");

        databaseHelper.addObject(INETNUM_NON_RIPE_200_8);
        // authoritative-resource are not hierarchic but flat, so no entry created for children
        authoritativeResourceDao.create("arin", "200.0.0.0-200.255.255.255");

        databaseHelper.addObject(ROUTE_UNDER_200_0_16_X);
        databaseHelper.addObject(ROUTE_UNDER_200_0_16_Y);

        databaseHelper.addObject(INETNUM_NON_RIPE_UNDER_200_0_16_X);
        databaseHelper.addObject(INETNUM_NON_RIPE_UNDER_200_0_16_Y);

        databaseHelper.addObject(DOMAIN_UNDER_200_0_16_X);
        databaseHelper.addObject(DOMAIN_UNDER_200_0_16_Y);

        databaseHelper.addObject(INETNUM_RIPE_193_8);
        authoritativeResourceDao.create("test", "193.0.0.0-193.255.255.255");
        databaseHelper.addObject(INETNUM_RIPE_193_0_16);
        databaseHelper.addObject(INETNUM_NON_RIPE_193_0_0_24);

        databaseHelper.addObject(INETNUM_NON_RIPE_201_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_201_0_16);
        databaseHelper.addObject(INETNUM_NON_RIPE_201_0_0_24);

        databaseHelper.addObject(INETNUM_NON_RIPE_202_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_203_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_204_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_205_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_206_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_207_8);

        ipTreeUpdater.rebuild();
    }


    @Test
    public void it_should_delete_placeholder_if_it_is_an_exact_match() {
        description:
        // main transfer-in scenario:
        // transferred region exactly matches existing non-ripe placeholder

        given:
        assertThat(inetnumWithNetnameExists("200.0.0.0-200.255.255.255", InetnumTransfer.NON_RIPE_NETNAME), is(true));
        assertThat(isMaintainedInRirSpace("200.0.0.0-200.255.255.255"), is(false));

        when:
        transferIn("200.0.0.0/8");

        then:
        assertThat(inetNumExists("200.0.0.0-200.255.255.255"), is(false));
        assertThat(isMaintainedInRirSpace("200.0.0.0-200.255.255.255"), is(true));
    }

    @Test
    public void it_should_create_placeholder_for_what_is_left_from_original_inetnum() {
        description:
        // main transfer-in scenario:
        // transferred region matches with less-specific non-ripe placeholder

        given:
        assertThat(inetnumWithNetnameExists("200.0.0.0-200.255.255.255", InetnumTransfer.NON_RIPE_NETNAME), is(true));
        assertThat(isMaintainedInRirSpace("200.0.0.0-200.255.255.255"), is(false));

        when:
        transferIn("200.0.0.0/9");

        then:
        assertThat(inetNumExists("200.0.0.0-200.255.255.255"), is(false));
        assertThat(inetnumWithNetnameExists("200.128.0.0/9", InetnumTransfer.NON_RIPE_NETNAME), is(true));
        assertThat(isMaintainedInRirSpace("200.0.0.0-200.255.255.255"), is(false));
        assertThat(isMaintainedInRirSpace("200.0.0.0-200.127.255.255"), is(true));
    }

    @Test
    public void it_should_not_modify_0_0() {
        transferIn("200.0.0.0/9");

        assertThat(inetNumExists("0.0.0.0/0"), is(true));
    }

    @Test
    public void it_should_delete_part_of_the_placeholder_that_belongs_to_incoming_block() {
        transferIn("200.0.0.0/9");

        assertThat(inetNumExists("200.0.0.0/9"), is(false));
    }

    @Test
    public void it_should_delete_part_of_the_parent_placeholder_that_belongs_to_incoming_block() {
        transferIn("200.0.0.0/16");

        assertThat(inetNumExists("200.0.0.0 - 200.255.255.255"), is(false));
    }

    @Test
    public void it_should_delete_non_ripe_placeholder_when_transferring_it_back_in() {
        transferIn("200.0.0.0/16");

        assertThat(inetNumExists("200.0.0.0/16"), is(false));
    }

    @Test
    public void it_should_create_parent_placeholder_for_what_is_left_from_original_parent_inetnum() {
        transferIn("200.0.0.0/16");

        assertThat(inetnumWithNetnameExists("200.1.0.0 - 200.255.255.255", InetnumTransfer.NON_RIPE_NETNAME), is(true));
        assertThat(isMaintainedInRirSpace("200.0.0.0 - 200.0.255.255"), is(false));
    }

    @Test
    public void it_should_not_delete_routes_under_transferred_inetnum() {
        transferIn("200.0.0.0/16");

        assertThat(objectExists(ObjectType.ROUTE, "200.0.0.0/17AS12345666"), is(true));
        assertThat(objectExists(ObjectType.ROUTE, "200.0.0.0/18AS66654321"), is(true));
    }

    @Test
    public void it_should_not_delete_inetnums_under_transferred_inetnum() {
        transferIn("200.0.0.0/16");

        assertThat(objectExists(ObjectType.INETNUM, "200.0.128.0 - 200.0.128.255"), is(true));
        assertThat(objectExists(ObjectType.INETNUM, "200.0.128.129 - 200.0.128.255"), is(true));
    }

    @Test
    public void it_should_not_delete_domains_under_transferred_inetnum() {
        transferIn("200.0.0.0/16");

        assertThat(objectExists(ObjectType.DOMAIN, "6.0.200.in-addr.arpa"), is(true));
        assertThat(objectExists(ObjectType.DOMAIN, "7.0.200.in-addr.arpa"), is(true));
    }

    @Test
    public void it_should_not_modify_ripe_inetnums_with_a_non_ripe_inetnum_under_it() {
        transferIn("193.0.0.0/24");

        assertThat(inetNumExists("193.0.0.0/8"), is(true));
        assertThat(inetNumExists("193.0.0.0/16"), is(true));
    }

    @Test
    public void it_should_not_modify_non_placeholders() {
        transferIn("201.0.0.0/24");

        assertThat(inetNumExists("201.0.0.0/24"), is(true));
    }

    @Test
    public void it_should_modify_1st_placeholder_only() {
        transferIn("201.0.0.0/24");

        assertThat(inetNumExists("201.0.0.0/16"), is(false));
    }

    @Test
    public void it_should_not_modify_placeholders_after_1st_layer() {
        transferIn("201.0.0.0/24");

        assertThat(inetNumExists("201.0.0.0/24"), is(true));
    }


    @Test
    public void it_should_report_authentication_error() {
        description:  // should report error thrown deeply from with transaction correctly

        try {
            transferIn("200.0.0.0/8", "nonExistingUser,dummyPassword,noreason");
            fail();
        } catch(NotAuthorizedException exc) {
            assertThat(exc.getResponse().readEntity(String.class), containsString("FAILED_AUTHENTICATION") );
        }
    }

    @Test
    public void it_should_modify_placeholders_in_sequence() throws InterruptedException {

        asynchronousTransfer("202.116.0.0/14");
        ipTreeUpdater.updateTransactional();
        asynchronousTransfer("202.120.0.0/14");
        asynchronousTransfer("202.140.0.0/14");
        asynchronousTransfer("202.168.0.0/14");
        asynchronousTransfer("202.172.0.0/14");
        asynchronousTransfer("202.176.0.0/14");
        asynchronousTransfer("202.180.0.0/14");
        asynchronousTransfer("202.240.0.0/14");

        asynchronousTransfer("203.116.0.0/14");
        asynchronousTransfer("203.120.0.0/14");
        asynchronousTransfer("203.140.0.0/14");
        asynchronousTransfer("203.168.0.0/14");
        asynchronousTransfer("203.172.0.0/14");
        asynchronousTransfer("203.176.0.0/14");
        asynchronousTransfer("203.180.0.0/14");
        asynchronousTransfer("203.240.0.0/14");


        asynchronousTransfer("204.116.0.0/14");
        asynchronousTransfer("204.120.0.0/14");
        asynchronousTransfer("204.140.0.0/14");
        asynchronousTransfer("204.168.0.0/14");
        asynchronousTransfer("204.172.0.0/14");
        asynchronousTransfer("204.176.0.0/14");
        asynchronousTransfer("204.180.0.0/14");
        asynchronousTransfer("204.240.0.0/14");


        asynchronousTransfer("205.116.0.0/14");
        asynchronousTransfer("205.120.0.0/14");
        asynchronousTransfer("205.140.0.0/14");
        asynchronousTransfer("205.168.0.0/14");
        asynchronousTransfer("205.172.0.0/14");
        asynchronousTransfer("205.176.0.0/14");
        asynchronousTransfer("205.180.0.0/14");
        asynchronousTransfer("205.240.0.0/14");


        asynchronousTransfer("206.116.0.0/14");
        asynchronousTransfer("206.120.0.0/14");
        asynchronousTransfer("206.140.0.0/14");
        asynchronousTransfer("206.168.0.0/14");
        asynchronousTransfer("206.172.0.0/14");
        asynchronousTransfer("206.176.0.0/14");
        asynchronousTransfer("206.180.0.0/14");
        asynchronousTransfer("206.240.0.0/14");


        asynchronousTransfer("207.116.0.0/14");
        asynchronousTransfer("207.120.0.0/14");
        asynchronousTransfer("207.140.0.0/14");
        asynchronousTransfer("207.168.0.0/14");
        asynchronousTransfer("207.172.0.0/14");
        asynchronousTransfer("207.176.0.0/14");
        asynchronousTransfer("207.180.0.0/14");
        asynchronousTransfer("207.240.0.0/14");
    }

    private void asynchronousTransfer(String range) {
        ((Runnable) () -> transferIn(range)).run();
    }

    private void transferIn(String inetnum, final String overrideLine ) {
        try {
            WhoisResources resp = RestTest.target(getPort(), "whois/transfer/inetnum/",
                    "override=" + SyncUpdateUtils.encode(overrideLine), null)
                    .path(URLEncoder.encode(inetnum, "UTF-8"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.text(null), WhoisResources.class);
            printErrorMessage(resp);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        } catch (ClientErrorException exc) {
            printErrorMessage(exc.getResponse().readEntity(WhoisResources.class));
            throw exc;
        }
    }

    private void transferIn(String inetnum) {
        transferIn(inetnum, OVERRIDE_LINE);
    }

}
