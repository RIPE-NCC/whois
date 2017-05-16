package net.ripe.db.whois.api.transfer.inetnum;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.api.transfer.logic.inetnum.InetnumTransfer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@Category(IntegrationTest.class)
public class InetnumTransferErrorsServiceIntegrationTest extends AbstractInetnumTransferInternalTest {

    private static final String NON_RIPE_INETNUM_TEMPLATE = "" +
            "inetnum:        %s\n" +
            "netname:        " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "country:        EU # Country is really world wide\n" +
            "org:            ORG-IANA1-RIPE\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "mnt-lower:      RIPE-NCC-HM-MNT\n" +
            "mnt-routes:     RIPE-NCC-RPSL-MNT\n" +
            "created:        2014-11-07T14:14:58Z\n" +
            "last-modified:  2014-11-07T14:14:58Z\n" +
            "source:         TEST";
    private static final String RIPE_INETNUM_TEMPLATE = "" +
            "inetnum:         %s\n" +
            "netname:         RIPE-NCC\n" +
            "descr:           RIPE Network Coordination Centre\n" +
            "descr:           Amsterdam, Netherlands\n" +
            "country:         NL\n" +
            "admin-c:         PERSON-TEST\n" +
            "tech-c:          PERSON-TEST\n" +
            "status:          ASSIGNED PI\n" +
            "mnt-by:          RIPE-NCC-MNT\n" +
            "created:         2003-03-17T12:15:57Z\n" +
            "last-modified:   2015-03-04T16:35:39Z\n" +
            "source:          TEST";
    private static final String RIPE_PLACEHOLDER_TEMPLATE = "" +
            "inetnum:         %s\n" +
            "netname:         EU-ZZ-185\n" +
            "descr:           RIPE NCC\n" +
            "descr:           European Regional Registry\n" +
            "country:         EU\n" +
            "org:             ORG-NCC1-RIPE\n" +
            "admin-c:         PERSON-TEST\n" +
            "tech-c:          PERSON-TEST\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "mnt-lower:       RIPE-NCC-HM-MNT\n" +
            "created:         2011-02-08T09:10:20Z\n" +
            "last-modified:   2011-02-08T09:10:20Z\n" +
            "source:          TEST\n";
    private static final String NON_RIPE_PLACEHOLDER_TEMPLATE = "" +
            "inetnum:         %s\n" +
            "netname:         " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:           IPv4 address block not managed by the RIPE NCC\n" +
            "country:         EU # Country is really world wide\n" +
            "org:             ORG-IANA1-RIPE\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "mnt-lower:       RIPE-NCC-HM-MNT\n" +
            "mnt-routes:      RIPE-NCC-RPSL-MNT\n" +
            "created:         2014-11-07T14:14:51Z\n" +
            "last-modified:   2014-11-07T14:14:51Z\n" +
            "source:          TEST\n";
    private static final String INETNUM_IANA = "" +
            "inetnum:         10.0.0.0 - 10.255.255.255\n" +
            "netname:         " + InetnumTransfer.IANA_NETNAME + "\n" +
            "descr:           IPv4 address block reserved by the IETF\n" +
            "country:         EU # Country is really world wide\n" +
            "org:             ORG-IANA1-RIPE\n" +
            "admin-c:         PERSON-TEST\n" +
            "tech-c:          PERSON-TEST\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "created:         2014-11-07T14:27:58Z\n" +
            "last-modified:   2014-11-07T14:38:18Z\n" +
            "source:          TEST # Filtered\n";

    @Before
    public void setUpEach() {
        ipTreeUpdater.rebuild();
    }

    @Test
    public void transfer_in_should_detect_garbage_input() {
        given:

        when:
        then:
        transferInWithResult("4567",
                400, "Inetnum 4567 has invalid syntax.");

    }

    @Test
    public void transfer_out_should_detect_garbage_input() {
        given:

        when:
        then:
        transferOutWithResult("321",
                400, "Inetnum 321 has invalid syntax.");

    }

    @Test
    public void transfer_in_should_detect_garbage_with_slash_input() {
        given:

        when:
        then:
        transferInWithResult("garbage/11",
                400, "Inetnum garbage/11 has invalid syntax.");

    }

    @Test
    public void transfer_out_should_detect_garbage_with_slash_input() {
        given:

        when:
        then:
        transferOutWithResult("garbage/11",
                400, "Inetnum garbage/11 has invalid syntax.");

    }

    @Test
    public void transfer_in_should_allow_dash_notation() {
        given:
        databaseHelper.addObject(createResource("NON-RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("193.0.0.0-193.255.255.255",
                200, "Successfully transferred in inetnum 193.0.0.0-193.255.255.255");

    }

    @Test
    public void transfer_in_should_not_allow_short_slash_notation() {
        given:
        databaseHelper.addObject(createResource("NON-RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("193/8",
                400, "Invalid IPv4 address: 193");

    }

    @Test
    public void transfer_out_should_allow_dash_notation() {
        given:
        databaseHelper.addObject(createResource("RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferOutWithResult("193.0.0.0-193.255.255.254",
                200, "Successfully transferred out inetnum 193.0.0.0-193.255.255.254");
    }

    @Test
    public void transfer_out_should_not_allow_short_slash_notation() {
        given:
        databaseHelper.addObject(createResource("RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferOutWithResult("193/8",
                400, "Invalid IPv4 address: 193");

    }

    @Test
    public void transfer_in__should_report_non_existing_resource() {
        given:

        when:
        then:
        transferInWithResult("193.0.0.0/8",
                404, "Inetnum 193.0.0.0/8 not found.");
    }

    @Test
    public void transfer_out_should_report_non_existing_resource() {
        given:


        when:
        then:
        transferOutWithResult("193.0.0.0/8",
                404, "Inetnum 193.0.0.0/8 not found.");
    }

    @Test
    public void transfer_in_should_not_allow_transfer_into_iana() {
        given:
        databaseHelper.addObject(INETNUM_IANA);
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("10.0.0.0/8",
                400, "Inetnum 10.0.0.0/8 is owned by IANA.");
    }

    @Test
    public void transfer_out_should_not_allow_transfer_into_iana() {
        given:
        databaseHelper.addObject(INETNUM_IANA);
        ipTreeUpdater.rebuild();

        when:
        then:
        transferOutWithResult("10.0.0.0/8",
                400, "Inetnum 10.0.0.0/8 is owned by IANA.");

    }

    @Test
    public void transfer_in_should_detect_transfer_crossing_boundary_on_transfer_in() {
        given:
        databaseHelper.addObject(createResource("RIPE", "193.0.0.0 - 193.127.255.255"));
        databaseHelper.addObject(createResource("NON-RIPE", "193.128.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("193.0.0.0/8",
                404, "Inetnum 193.0.0.0/8 not found.");
    }

    @Test
    public void transfer_out_should_detect_transfer_crossing_boundary_on_transfer_in() {
        given:
        databaseHelper.addObject(createResource("RIPE", "193.0.0.0 - 193.127.255.255"));
        databaseHelper.addObject(createResource("NON-RIPE", "193.128.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();


        when:
        then:
        transferOutWithResult("193.0.0.0/8",
                404, "Inetnum 193.0.0.0/8 not found.");
    }

    @Test
    public void transfer_in_should_noop_transfer_ripe_into_ripe() {
        given:
        databaseHelper.addObject(createPlaceholder("RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("193.0.0.0/8",
                200, "Inetnum 193.0.0.0/8 is already RIPE.");

    }

    @Test
    public void transfer_out_should_noop_transfer_ripe_into_ripe() {
        given:
        databaseHelper.addObject(createPlaceholder("NON-RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferOutWithResult("193.0.0.0-193.0.0.255",
                200, "Inetnum 193.0.0.0-193.0.0.255 is already non-RIPE.");

    }

    @Test
    public void transfer_in_should_succeed() {
        given:
        databaseHelper.addObject(createResource("NON-RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferInWithResult("193.0.0.0/8",
                200, "Successfully transferred in inetnum 193.0.0.0/8");
    }

    @Test
    public void transfer_out_should_succeed() {
        given:
        databaseHelper.addObject(createResource("RIPE", "193.0.0.0 - 193.255.255.255"));
        ipTreeUpdater.rebuild();

        when:
        then:
        transferOutWithResult("193.0.0.0-193.0.0.255",
                200, "Successfully transferred out inetnum 193.0.0.0-193.0.0.255");
    }

    private void transferInWithResult(final String inetnum, final int status, final String message) {
        try {
            WhoisResources resp = RestTest.target(getPort(), "whois/transfer/inetnum/",
                    "override=" + SyncUpdateUtils.encode(OVERRIDE_LINE), null)
                    .path(URLEncoder.encode(inetnum, "UTF-8"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.text(null), WhoisResources.class);
            printErrorMessage(resp);

            assertThat(200, is(status));
            assertThat(getInfoMessage(resp), is(message));

        } catch (ClientErrorException exc) {
            printErrorMessage(exc.getResponse().readEntity(WhoisResources.class));

            assertThat(exc.getResponse().getStatus(), is(status));
            assertThat(getErrorMessage(exc.getResponse().readEntity(WhoisResources.class)), is(message));

        } catch (Exception exc) {

            fail(exc.getMessage());
        }
    }

    private void transferOutWithResult(final String inetnum, final int status, final String message) {
        try {
            WhoisResources resp = RestTest.target(getPort(), "whois/transfer/inetnum/",
                    "override=" + SyncUpdateUtils.encode(OVERRIDE_LINE), null)
                    .path(URLEncoder.encode(inetnum, "UTF-8"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .delete(WhoisResources.class);

            assertThat(200, is(status));
            assertThat(getInfoMessage(resp), is(message));

        } catch (ClientErrorException exc) {
            printErrorMessage(exc.getResponse().readEntity(WhoisResources.class));

            assertThat(exc.getResponse().getStatus(), is(status));
            assertThat(getErrorMessage(exc.getResponse().readEntity(WhoisResources.class)), is(message));

        } catch (Exception exc) {

            fail(exc.getMessage());
        }
    }

    private String createPlaceholder(final String source, final String range) {
        if ("RIPE".equalsIgnoreCase(source)) {
            return String.format(RIPE_PLACEHOLDER_TEMPLATE, range);
        }
        return String.format(NON_RIPE_PLACEHOLDER_TEMPLATE, range);
    }

    private String createResource(final String source, final String range) {
        if ("RIPE".equalsIgnoreCase(source)) {
            return String.format(RIPE_INETNUM_TEMPLATE, range);
        }
        return String.format(NON_RIPE_INETNUM_TEMPLATE, range);
    }

}
