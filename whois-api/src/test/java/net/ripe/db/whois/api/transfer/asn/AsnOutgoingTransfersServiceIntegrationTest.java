package net.ripe.db.whois.api.transfer.asn;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.api.transfer.AbstractTransferTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceDao;
import net.ripe.db.whois.api.transfer.logic.asn.AsnTransfer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class AsnOutgoingTransfersServiceIntegrationTest extends AbstractAsnTransferInternalTest {
    private static final String AS1_____far_first_of_ripe_block = "AS1";
    private static final String AS10____first_of_ripe_block = "AS10";
    private static final String AS15____middle_in_ripe_block = "AS15";
    private static final String AS19____last_of_ripe_block = "AS19";
    private static final String AS59____far_last_of_ripe_block = "AS59";
    private static final String AS50___ripe_block_next_to_iana_block = "AS50";
    private static final String AS44____in_iana_block = "AS44";
    private static final String AS9_____in_non_ripe_block = "AS9";
    private static final String AS99____unknown_block = "AS99";

    @Autowired
    private AuthoritativeResourceDao authoritativeResourceDao;

    @Before
    public void setUpEach() {

        databaseHelper.addObject("" +
                "as-block:      AS1 - AS4\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS5 - AS9\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS10 - AS19\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS20 - AS29\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS30 - AS39\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS40 - AS49\n" +
                "descr:         " + AsnTransfer.IANA_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS50 - AS59\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        authoritativeResourceDao.create("test", "AS1-AS4");
        authoritativeResourceDao.create("arin", "AS5-AS9");
        authoritativeResourceDao.create("test", "AS10-AS19");
        authoritativeResourceDao.create("arin", "AS20-AS29");
        authoritativeResourceDao.create("test", "AS30-AS39");
        authoritativeResourceDao.create("test", "AS50-AS59");
    }

    // far-first-section: start
    @Test
    public void transfer_out_block_of_size_one() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS139"), is(false));
    }

    @Test
    public void transfer_out_block_of_size_one_with_no_preceding_block() {

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS139"), is(false));
    }

    @Test
    public void transfer_out_block_of_size_one_with_no_following_block() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS120"), is(false));
    }

    @Test
    public void transfer_out_block_of_size_one_with_no_following_and_no_preceding_block() {
        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS120"), is(false));
    }

    @Test
    public void transfer_out_block_of_size_one_with_iana_following_block() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");


        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.IANA_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS120"), is(false));
    }

    @Test
    public void transfer_out_block_of_size_one_with_iana_preceding_block() {

        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.IANA_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferOut("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS139"), is(false));
    }

    @Test
    public void transfer_out_far_first_of_ripe_block__original_not_exists() {
        transferOut(AS1_____far_first_of_ripe_block);

        assertThat(objectExists("AS1 - AS4"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS2-AS4"), is(true));
    }

    @Test
    public void transfer_out_far_begin_of_ripe_block__new_left_exists() {
        transferOut(AS1_____far_first_of_ripe_block);

        assertThat(objectExists("AS1 - AS1"), is(true));
        assertThat(isRipeAsBlock("AS1 - AS1"), is(false));
    }

    @Test
    public void transfer_out_far_begin_of_ripe_block__shrunken_original_exists() {
        transferOut(AS1_____far_first_of_ripe_block);

        assertThat(isRipeAsBlock("AS2 - AS4"), is(true));
    }

    // far-first-section: end

    // first-section: start

    @Test
    public void transfer_out_begin_of_ripe_block__original_not_exists() {
        transferOut(AS10____first_of_ripe_block);

        assertThat(objectExists("AS10 - AS19"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS11-AS19"), is(true));
    }

    @Test
    public void transfer_out_begin_of_ripe_block__shrunken_original_exists() {
        transferOut(AS10____first_of_ripe_block);

        assertThat(objectExists("AS11 - AS19"), is(true));
        assertThat(isRipeAsBlock("AS11 - AS19"), is(true));
    }

    @Test
    public void transfer_out_begin_of_ripe_block__original_left_not_exists() {
        transferOut(AS10____first_of_ripe_block);

        assertThat(objectExists("AS5 - AS9"), is(false));
    }

    @Test
    public void transfer_out_begin_of_ripe_block__extended_left_exists() {
        transferOut(AS10____first_of_ripe_block);

        assertThat(objectExists("AS5 - AS10"), is(true));
        assertThat(isRipeAsBlock("AS5 - AS10"), is(false));
    }

    // first-section: end

    // middle-section start

    @Test
    public void transfer_out_middle_of_ripe_block__original_not_exists() {
        transferOut(AS15____middle_in_ripe_block);

        assertThat(objectExists("AS10 - AS19"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS10-AS14"), is(true));
        assertThat(isMaintainedInRirSpace("test", "AS16-AS19"), is(true));
    }

    @Test
    public void transfer_out_middle_of_ripe_block__new_middle_exists() {
        transferOut(AS15____middle_in_ripe_block);

        assertThat(objectExists("AS15 - AS15"), is(true));
        assertThat(isRipeAsBlock("AS15 - AS15"), is(false));
    }

    @Test
    public void transfer_out_middle_of_ripe_block__new_left_exists() {
        transferOut(AS15____middle_in_ripe_block);

        assertThat(isRipeAsBlock("AS10 - AS14"), is(true));
        assertThat(isRipeAsBlock("AS15 - AS15"), is(false));
        assertThat(isRipeAsBlock("AS16 - AS19"), is(true));
    }

    @Test
    public void transfer_out_middle_of_ripe_block__new_right_exists() {
        transferOut(AS15____middle_in_ripe_block);

        assertThat(objectExists("AS16 - AS19"), is(true));
        assertThat(isRipeAsBlock("AS16 - AS19"), is(true));
    }

    // middle-section: end

    // last-section: start

    @Test
    public void transfer_out_last_of_ripe_block__original_not_exists() {
        transferOut(AS19____last_of_ripe_block);

        assertThat(objectExists("AS10 - AS19"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS10-AS18"), is(true));
    }

    @Test
    public void transfer_out_end_of_ripe_block__shrunken_original_exists() {
        transferOut(AS19____last_of_ripe_block);

        assertThat(objectExists("AS10 - AS18"), is(true));
        assertThat(isRipeAsBlock("AS10 - AS18"), is(true));
    }

    @Test
    public void transfer_out_last_of_ripe_block__original_right_not_exists() {
        transferOut(AS19____last_of_ripe_block);

        assertThat(isRipeAsBlock("AS10 - AS18"), is(true));
        assertThat(isRipeAsBlock("AS19 - AS29"), is(false));
        assertThat(objectExists("AS20 - AS29"), is(false));
    }

    @Test
    public void transfer_out_last_of_ripe_block__extended_right_exists() {
        transferOut(AS19____last_of_ripe_block);

        assertThat(objectExists("AS19 - AS29"), is(true));
        assertThat(isRipeAsBlock("AS19 - AS29"), is(false));
    }

    // last-section: end

    // far-last section: start

    @Test
    public void transfer_out_far_last_of_ripe_block__original_not_exists() {
        transferOut(AS59____far_last_of_ripe_block);

        assertThat(objectExists("AS50 - AS59"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS50-AS58"), is(true));
    }

    @Test
    public void transfer_out_far_end_of_ripe__block__new_right_created() {
        transferOut(AS59____far_last_of_ripe_block);

        assertThat(objectExists("AS59 - AS59"), is(true));
        assertThat(isRipeAsBlock("AS59 - AS59"), is(false));
    }

    @Test
    public void transfer_out_far_end_of_ripe_block__shrunken_original_exists() {
        transferOut(AS59____far_last_of_ripe_block);

        assertThat(objectExists("AS50 - AS58"), is(true));
        assertThat(isRipeAsBlock("AS50 - AS58"), is(true));
    }

    @Test
    public void transfer_in_authentication_error() {
        try {
            transferOut(AS59____far_last_of_ripe_block, "nonExistingUser.wrongPassword,dummy topic");
        } catch(NotAuthorizedException exc) {
            assertThat(exc.getResponse().readEntity(String.class), containsString("FAILED_AUTHENTICATION") );
        }
    }

    // far-last section: end

    @Test
    public void transfer_out_iana_number() {
        try {
            transferOut(AS44____in_iana_block);
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("IANA blocks are not available for transfers: AS44"));
        }
    }

    @Test
    public void transfer_out_next_to_iana_block() {
        transferOut(AS50___ripe_block_next_to_iana_block);

        assertThat(objectExists("AS50 - AS59"), is(false));

        assertThat(objectExists("AS40 - AS49"), is(true));
        assertThat(isIanaAsBlock("AS40 - AS49"), is(true));

        assertThat(objectExists("AS51 - AS59"), is(true));
        assertThat(isRipeAsBlock("AS51 - AS59"), is(true));

        assertThat(objectExists("AS50 - AS50"), is(true));
        assertThat(isNonRipeAsBlock("AS50 - AS50"), is(true));
    }

    @Test
    public void transfer_out_non_ripe_number() {
        assertThat(transferOut(AS9_____in_non_ripe_block),
                containsString("Resource " + AS9_____in_non_ripe_block + " is already non-RIPE"));
    }

    @Test
    public void transfer_out_non_existing_number() {
        try {
            transferOut(AS99____unknown_block);
            fail();
        } catch (NotFoundException e) {
            assertThat(getResponseBody(e), containsString("Block not found"));
        }
    }

    // error cases

    @Test
    public void transfer_out_syntax_error() {
        try {
            transferOut("AS*");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Invalid AS number: 'AS*'"));
        }
    }


    @Test
    public void transfer_asn_from_unknown_region() {

        databaseHelper.addObject("" +
                "as-block:      AS241 - AS250\n" +
                "descr:         some unknown region\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        try {
            transferOut("AS241");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Block is not recognizable: AS241 - AS250"));
        }
    }

    @Test
    public void transfer_out_detect_left_neighbour_to_be_merged_first() {
        given:
        databaseHelper.addObject("" +
                "as-block:      AS60-AS69\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:      AS70-AS79\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        when:
        then:
        try {
            transferOut("AS70");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Adjacent block AS60-AS69 should be merged with current block AS70-AS79 first"));
        }
    }

    @Test
    public void transfer_out_detect_right_neighbour_to_be_merged_first() {
        given:
        databaseHelper.addObject("" +
                "as-block:      AS60-AS69\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:      AS70-AS79\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        when:
        then:
        try {
            transferOut("AS69");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Adjacent block AS70-AS79 should be merged with current block AS60-AS69 first"));
        }
    }


    // helper methods

    private String transferOut(final String asNum) {
        return transferOut(asNum, AbstractTransferTest.OVERRIDE_LINE);
    }

    private String transferOut(final String asNum, final String overrideLine) {
        try {
            final WhoisResources whoisResources = RestTest.target(getPort(), "whois/transfer/aut-num/",
                    "override=" + SyncUpdateUtils.encode(overrideLine), null)
                    .path(URLEncoder.encode(asNum, "UTF-8"))
                    .request()
                    .delete(WhoisResources.class);
            return whoisResources.getErrorMessages().toString();
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return null;
        }
    }

}
