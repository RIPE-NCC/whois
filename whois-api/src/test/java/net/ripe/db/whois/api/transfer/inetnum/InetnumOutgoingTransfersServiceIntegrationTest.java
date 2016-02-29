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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@Category(IntegrationTest.class)
public class InetnumOutgoingTransfersServiceIntegrationTest extends AbstractInetnumTransferInternalTest {

    private static final String INETNUM_RIPE_191_8 = "" +
            "inetnum:         191.0.0.0 - 191.255.255.255\n" +
            "netname:         EU-ZZ-191\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_NON_RIPE_191_0_LEFT = "" +
            "inetnum:         191.0.0.0 - 191.0.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_191_0_RIGHT = "" +
            "inetnum:         191.1.0.255 - 191.255.255.254\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_NON_RIPE_188_255_255_24 = "" +
            "inetnum:         188.255.255.0 - 188.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_RIPE_189_8 = "" +
            "inetnum:         189.0.0.0 - 189.255.255.255\n" +
            "netname:         EU-ZZ-189\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_NON_RIPE_189_0_RIGHT = "" +
            "inetnum:         189.128.0.0 - 189.255.255.255\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_RIPE_173_8 = "" +
            "inetnum:         173.0.0.0 - 173.255.255.255\n" +
            "netname:         EU-ZZ-173\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_RIPE_173_0_LEFT = "" +
            "inetnum:         173.0.0.0 - 173.0.255.255\n" +
            "netname:        Some netname\n" +
            "descr:          IPv4 address block managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_RIPE_173_0_RIGHT = "" +
            "inetnum:         173.1.0.255 - 173.255.255.254\n" +
            "netname:        Some netname\n" +
            "descr:          IPv4 address block  managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         TEST";
    private static final String INETNUM_RIPE_194_8 = "" +
            "inetnum:         194.0.0.0 - 194.255.255.255\n" +
            "netname:         EU-ZZ-194\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_RIPE_194_0_16 = "" +
            "inetnum:         194.0.0.0 - 194.0.255.255\n" +
            "netname:         EU-ZZ-194\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_RIPE_194_0_0_24 = "" +
            "inetnum:         194.0.0.0 - 194.0.0.255\n" +
            "netname:         EU-ZZ-194\n" +
            "descr:           IPv4 address block managed by the RIPE NCC\n" +
            "country:         EU # Country is in fact world wide\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String DOMAIN_UNDER_194_0_16_X = "" +
            "domain:          6.0.194.in-addr.arpa\n" +
            "descr:           RIPE NCC Internal Use\n" +
            "admin-c:         PERSON-TEST\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    private static final String INETNUM_UNDER_194_0_16_X = "" +
            "inetnum:         194.0.128.0 - 194.0.128.255\n" +
            "netname:         EU-ZZ-UNDER-16\n" +
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
    private static final String ROUTE_UNDER_194_0_16_X = "" +
            "route:           194.0.0.0/17\n" +
            "descr:           Route X\n" +
            "origin:          AS12345666\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          TEST";
    @Autowired
    private AuthoritativeResourceDao authoritativeResourceDao;

    @Before
    public void setUpEach() {
        authoritativeResourceDao.delete("test", "0.0.0.0/0");

        databaseHelper.addObject(INETNUM_RIPE_194_8);
        // authoritative-resource are not hierarchic but flat, so no entry created for children
        authoritativeResourceDao.create("TEST", "194.0.0.0-194.255.255.255");

        databaseHelper.addObject(INETNUM_RIPE_194_0_16);
        databaseHelper.addObject(INETNUM_RIPE_194_0_0_24);

        databaseHelper.addObject(ROUTE_UNDER_194_0_16_X);
        databaseHelper.addObject(INETNUM_UNDER_194_0_16_X);
        databaseHelper.addObject(DOMAIN_UNDER_194_0_16_X);

        databaseHelper.addObject(INETNUM_RIPE_191_8);
        authoritativeResourceDao.create("TEST", "191.1.0.0-191.1.0.254");

        databaseHelper.addObject(INETNUM_NON_RIPE_191_0_LEFT);
        authoritativeResourceDao.create("arin", "191.0.0.0-191.0.255.255");
        databaseHelper.addObject(INETNUM_NON_RIPE_191_0_RIGHT);
        authoritativeResourceDao.create("arin", "191.1.0.255-191.255.255.254");

        databaseHelper.addObject(INETNUM_NON_RIPE_188_255_255_24);

        databaseHelper.addObject(INETNUM_RIPE_189_8);
        databaseHelper.addObject(INETNUM_NON_RIPE_189_0_RIGHT);

        databaseHelper.addObject(INETNUM_RIPE_173_8);
        databaseHelper.addObject(INETNUM_RIPE_173_0_RIGHT);
        databaseHelper.addObject(INETNUM_RIPE_173_0_LEFT);

        ipTreeUpdater.rebuild();
    }

    @Test
    public void it_should_not_create_placeholder_for_exact_match_inetnum() {
        try {
            transferOut("194.0.0.0/24");
            fail();
        } catch (WebApplicationException e) {
            final String respMsg = getErrorMessage(e.getResponse().readEntity(WhoisResources.class));
            assertEquals("194.0.0.0/24 is an exact match and cannot be transferred out", respMsg);
        }
    }

    @Test
    public void it_should_create_placeholder_for_transferred_inetnum() {
        transferOut("194.0.0.0/32");

        assertThat(inetnumWithNetnameExists("194.0.0.0/32", InetnumTransfer.NON_RIPE_NETNAME), is(true));
    }

    @Test
    public void it_should_adjust_authoritive_resources() {
        given:
        assertThat(isMaintainedInRirSpace("194.0.0.0-194.255.255.255"), is(true));

        when:
        transferOut("194.0.0.0/32");

        then:
        assertThat(isMaintainedInRirSpace("194.0.0.0-194.255.255.255"), is(false));
        assertThat(isMaintainedInRirSpace("194.0.0.0-194.0.0.0"), is(false));
        assertThat(isMaintainedInRirSpace("194.0.0.1-194.255.255.255"), is(true));
    }

    @Test
    public void it_should_not_create_inenum_with_what_is_left_from_original() {
        transferOut("194.0.0.0/32");

        assertThat(inetNumExists("194.0.0.1 - 194.0.0.255"), is(false));
    }

    @Test
    public void it_should_not_delete_parent_inenums() {
        transferOut("194.0.0.0/32");

        assertThat(inetNumExists("194.0.0.0/24"), is(true));
        assertThat(inetNumExists("194.0.0.0/16"), is(true));
        assertThat(inetNumExists("194.0.0.0/8"), is(true));
        assertThat(inetNumExists("0.0.0.0/0"), is(true));
    }

    @Test
    public void it_should_not_delete_routes_under_transferred_inetnum() {
        transferOut("194.0.0.0-194.0.0.254");

        assertThat(objectExists(ObjectType.ROUTE, "194.0.0.0/17AS12345666"), is(true));
    }

    @Test
    public void it_should_not_delete_inetnums_under_transferred_inetnum() {
        transferOut("194.0.0.0-194.0.0.254");

        assertThat(objectExists(ObjectType.INETNUM, "194.0.128.0 - 194.0.128.255"), is(true));
    }

    @Test
    public void it_should_not_delete_domains_under_transferred_inetnum() {
        transferOut("194.0.0.0-194.0.0.254");

        assertThat(objectExists(ObjectType.DOMAIN, "6.0.194.in-addr.arpa"), is(true));
    }

    @Test
    public void it_should_delete_original_left_block_during_merge() {
        transferOut("191.1.0.0-191.1.0.128");

        assertThat(objectExists(ObjectType.INETNUM, "191.0.0.0 - 191.0.255.255"), is(false));
    }

    @Test
    public void it_should_merge_if_left_block_is_non_ripe() {
        transferOut("191.1.0.0-191.1.0.128");

        assertThat(inetnumWithNetnameExists("191.0.0.0 - 191.1.0.128", InetnumTransfer.NON_RIPE_NETNAME), is(true));
    }

    @Test
    public void it_should_delete_original_right_block_during_merge() {
        transferOut("191.1.0.128-191.1.0.254");

        assertThat(objectExists(ObjectType.INETNUM, "191.1.0.255 - 191.255.255.254"), is(false));
    }

    @Test
    public void it_should_merge_if_right_block_is_non_ripe() {
        transferOut("191.1.0.128-191.1.0.254");

        assertThat(inetnumWithNetnameExists("191.1.0.128-191.255.255.254", InetnumTransfer.NON_RIPE_NETNAME), is(true));
    }

    @Test
    public void it_should_delete_original_left_block_during_both_sides_merge() {
        transferOut("191.1.0.0-191.1.0.254");

        assertThat(objectExists(ObjectType.INETNUM, "191.0.0.0 - 191.0.255.255"), is(false));
    }

    @Test
    public void it_should_delete_original_right_block_during_both_sides_merge() {
        transferOut("191.1.0.0-191.1.0.254");

        assertThat(objectExists(ObjectType.INETNUM, "191.1.0.255 - 191.255.255.254"), is(false));
    }

    @Test
    public void it_should_merge_both_sides_if_blocks_are_non_ripe() {
        given:
        assertThat(inetNumExists("191.0.0.0-191.255.255.255"), is(true));
        assertThat(inetnumWithNetnameExists("191.0.0.0-191.0.255.255", InetnumTransfer.NON_RIPE_NETNAME), is(true));
        assertThat(inetNumExists("191.1.0.0-191.1.0.254"), is(false));
        assertThat(inetnumWithNetnameExists("191.1.0.255-191.255.255.254", InetnumTransfer.NON_RIPE_NETNAME), is(true));

        assertThat(isMaintainedInRirSpace("191.0.0.0-191.0.255.255"), is(false));
        assertThat(isMaintainedInRirSpace("191.1.0.0-191.1.0.254"), is(true));
        assertThat(isMaintainedInRirSpace("191.1.0.255-194.255.255.254"), is(false));

        when:
        transferOut("191.1.0.0-191.1.0.254");

        then:
        assertThat(inetnumWithNetnameExists("191.0.0.0-191.255.255.254", InetnumTransfer.NON_RIPE_NETNAME), is(true));

        // adjacent authoritive resource are not merged
        assertThat(isMaintainedInRirSpace("191.0.0.0-191.0.255.255"), is(false));
        assertThat(isMaintainedInRirSpace("191.1.0.0-191.1.0.254"), is(false));
        assertThat(isMaintainedInRirSpace("191.1.0.255-194.255.255.254"), is(false));

    }

    @Test
    public void it_should_not_merge_if_block_crosses_slash_8_boundaries() {
        transferOut("189.0.0.0-189.0.0.255");

        assertThat(inetnumWithNetnameExists("189.0.0.0-189.0.0.255", InetnumTransfer.NON_RIPE_NETNAME), is(true));
    }

    @Test
    public void it_should_not_merge_if_left_block_is_ripe() {
        transferOut("173.1.0.0-173.1.0.128");

        assertThat(objectExists(ObjectType.INETNUM, "173.0.0.0 - 173.1.0.128"), is(false));
    }

    @Test
    public void it_should_not_merge_if_right_block_is_ripe() {
        transferOut("173.1.0.128-173.1.0.254");

        assertThat(objectExists(ObjectType.INETNUM, "173.1.0.128-173.255.255.254"), is(false));
    }

    @Test
    public void it_is_merging_authoritive_resources() {
        transferOut("191.1.0.0-191.1.0.254");

        assertThat(inetnumWithNetnameExists("191.0.0.0-191.255.255.254", InetnumTransfer.NON_RIPE_NETNAME), is(true));
    }

    @Test
    public void it_should_report_authentication_error() {
        description:  // should report error thrown deeply from within transaction correctly

        try {
            transferOut("194.0.0.0/32", "nonExistingUser,dummyPassword,noreason");
            fail();
        } catch(NotAuthorizedException exc) {
            assertThat(exc.getResponse().readEntity(String.class), containsString("FAILED_AUTHENTICATION") );
        }
    }

    private void transferOut(String inetnum, final String overrideLine) {
        try {
            WhoisResources resp = RestTest.target(getPort(), "whois/transfer/inetnum/",
                    "override=" + SyncUpdateUtils.encode(overrideLine), null)
                    .path(URLEncoder.encode(inetnum, "UTF-8"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(WhoisResources.class);
            printErrorMessage(resp);
        } catch (ClientErrorException exc) {
            printErrorMessage(exc.getResponse().readEntity(WhoisResources.class));
            throw exc;
        } catch (WebApplicationException e) {
            fail(e.getResponse().readEntity(String.class));
            throw e;
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
    }
    private void transferOut(String inetnum) {
        transferOut(inetnum, OVERRIDE_LINE);
    }

}
