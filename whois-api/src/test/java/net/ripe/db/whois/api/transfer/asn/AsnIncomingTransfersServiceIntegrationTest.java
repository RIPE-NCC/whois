package net.ripe.db.whois.api.transfer.asn;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.api.transfer.AbstractTransferTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceDao;
import net.ripe.db.whois.api.transfer.logic.asn.AsnTransfer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@Category(IntegrationTest.class)
public class AsnIncomingTransfersServiceIntegrationTest extends AbstractAsnTransferInternalTest {
    private static final String AS1____far_first_of_nonripe_block = "AS1";
    private static final String AS5____middle_in_nonripe_block = "AS5";
    private static final String AS10___last_of_nonripe_block = "AS10";
    private static final String AS21___first_of_nonripe_block = "AS21";
    private static final String AS41___nonripe_block_next_to_iana_block = "AS41";
    private static final String AS50___far_last_of_nonripe_block = "AS50";
    private static final String AS33___in_iana_block = "AS33";
    private static final String AS11___in_ripe_block = "AS11";
    private static final String AS99___unknown_block = "AS99";

    @Autowired
    private AuthoritativeResourceDao authoritativeResourceDao;

    @Before
    public void setUpEach() {

        databaseHelper.addObject("" +
                "as-block:      AS1 - AS10\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS11 - AS20\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS21 - AS30\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS31 - AS40\n" +
                "descr:         " + AsnTransfer.IANA_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS41 - AS50\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        authoritativeResourceDao.create("arin", "AS1-AS10");
        authoritativeResourceDao.create("test", "AS11-AS20");
        authoritativeResourceDao.create("arin", "AS21-AS30");
        authoritativeResourceDao.create("arin", "AS41-AS50");
    }

    // start of far begin
    @Test
    public void transfer_in_block_of_size_one() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS139"), is(true));
    }

    @Test
    public void transfer_in_block_of_size_one_with_no_preceding_block() {

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");


        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS139"), is(true));
    }

    @Test
    public void transfer_in_block_of_size_one_with_no_following_block() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");


        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS120"), is(true));
    }

    @Test
    public void transfer_in_block_of_size_one_with_no_following_and_no_preceding_block() {
        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS120"), is(true));
    }

    @Test
    public void transfer_in_block_of_size_one_with_iana_following_block() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         IANA reserved ASN block\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS110 - AS120"), is(true));
        assertThat(isRipeAsBlock("AS110 - AS120"), is(true));
    }

    @Test
    public void transfer_in_block_of_size_one_with_iana_preceding_block() {
        databaseHelper.addObject("" +
                "as-block:      AS110 - AS119\n" +
                "descr:         " + AsnTransfer.IANA_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS120 - AS120\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "as-block:      AS121 - AS139\n" +
                "descr:         " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        transferIn("AS120");

        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS120"), is(false));
        assertThat(objectExists(ObjectType.AS_BLOCK, "AS120 - AS139"), is(true));
        assertThat(isRipeAsBlock("AS120 - AS139"), is(true));
    }

    @Test
    public void transfer_in_far_first_of_nonripe_block__original_not_exists() {
        transferIn(AS1____far_first_of_nonripe_block);

        assertThat(objectExists("AS1 - AS10"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS1-AS1"), is(true));
    }

    @Test
    public void transfer_in_far_first_of_nonripe_block__new_left_exists() {
        transferIn(AS1____far_first_of_nonripe_block);

        RpslObject asBlock = databaseHelper.lookupObject(ObjectType.AS_BLOCK, "AS1 - AS1");

        assertNotNull(asBlock);
        assertThat(isRipeAsBlock(asBlock), is(true));
    }

    @Test
    public void transfer_in_far_first_of_nonripe_block__shrunken_original_exists() {
        transferIn(AS1____far_first_of_nonripe_block);

        RpslObject asBlock = databaseHelper.lookupObject(ObjectType.AS_BLOCK, "AS2 - AS10");

        assertNotNull(asBlock);
        assertThat(isNonRipeAsBlock(asBlock), is(true));
    }

    // end of far begin

    // start of begin

    @Test
    public void transfer_in_begin_of_nonripe_block__original_not_exists() {
        transferIn(AS21___first_of_nonripe_block);

        assertThat(objectExists("AS11 - AS20"), is(false));
        assertThat(isMaintainedInRirSpace("test", "AS11-AS21"), is(true));
    }

    @Test
    public void transfer_in_begin_of_nonripe_block__shrunken_original_exists() {
        transferIn(AS21___first_of_nonripe_block);

        RpslObject asBlock = databaseHelper.lookupObject(ObjectType.AS_BLOCK, "AS22 - AS30");

        assertNotNull(asBlock);
        assertThat(isNonRipeAsBlock(asBlock), is(true));
    }

    @Test
    public void transfer_in_begin_of_nonripe_block__original_left_not_exists() {
        transferIn(AS21___first_of_nonripe_block);

        assertThat(objectExists("AS11 - AS20"), is(false));
    }

    @Test
    public void transfer_in_begin_of_nonripe_block__extended_left_exists() {
        transferIn(AS21___first_of_nonripe_block);

        assertThat(objectExists("AS11 - AS21"), is(true));
        assertThat(isRipeAsBlock("AS11 - AS21"), is(true));
    }

    // end of begin

    // Start of middle

    @Test
    public void transfer_in_middle_of_nonripe_block__original_not_exists() {
        transferIn(AS5____middle_in_nonripe_block);

        assertThat(objectExists("AS1 - AS10"), is(false));
        assertThat(isRipeAsBlock("AS5-AS5"), is(true));
    }

    @Test
    public void transfer_in_middle_of_nonripe_block__new_middle_exists() {
        transferIn(AS5____middle_in_nonripe_block);

        assertThat(objectExists("AS5 - AS5"), is(true));
        assertThat(isRipeAsBlock("AS5 - AS5"), is(true));
    }

    @Test
    public void transfer_in_middle_of_nonripe_block__new_left_exists() {
        transferIn(AS5____middle_in_nonripe_block);

        assertThat(objectExists("AS1 - AS4"), is(true));
        assertThat(isRipeAsBlock("AS1 - AS4"), is(false));
    }

    @Test
    public void transfer_in_middle_of_nonripe_block__new_right_exists() {
        transferIn(AS5____middle_in_nonripe_block);

        assertThat(objectExists("AS6 - AS10"), is(true));
        assertThat(isRipeAsBlock("AS6 - AS10"), is(false));
    }

    // end of middle

    // begin of last

    @Test
    public void transfer_in_last_of_nonripe_block__original_not_exists() {
        transferIn(AS10___last_of_nonripe_block);

        assertThat(objectExists("AS1 - AS10"), is(false));
        assertThat(isRipeAsBlock("AS10-AS20"), is(true));
    }

    @Test
    public void transfer_in_last_of_nonripe_block__shrunken_original_exists() {
        transferIn(AS10___last_of_nonripe_block);


        assertThat(objectExists("AS1 - AS9"), is(true));
        assertThat(isRipeAsBlock("AS1 - AS9"), is(false));
    }

    @Test
    public void transfer_in_last_of_nonripe_block__original_right_not_exists() {
        transferIn(AS10___last_of_nonripe_block);

        assertThat(objectExists("AS11 - AS20"), is(false));
    }

    @Test
    public void transfer_in_last_of_nonripe_block__extended_right_exists() {
        transferIn(AS10___last_of_nonripe_block);

        assertThat(objectExists("AS10 - AS20"), is(true));
        assertThat(isRipeAsBlock("AS10 - AS20"), is(true));
    }

    // end of last

    // start of far last

    @Test
    public void transfer_in_far_last_of_nonripe_block__original_not_exists() {
        transferIn(AS50___far_last_of_nonripe_block);

        assertThat(objectExists("AS41 - AS50"), is(false));
        assertThat(isRipeAsBlock("AS50-AS50"), is(true));
    }

    @Test
    public void transfer_in_far_last_of_nonripe_block__new_right_created() {
        transferIn(AS50___far_last_of_nonripe_block);

        assertThat(objectExists("AS50 - AS50"), is(true));
        assertThat(isRipeAsBlock("AS50 - AS50"), is(true));
    }

    @Test
    public void transfer_in_far_last_of_nonripe_block__shrunken_original_exists() {
        transferIn(AS50___far_last_of_nonripe_block);

        assertThat(objectExists("AS41 - AS49"), is(true));
        assertThat(isRipeAsBlock("AS41 - AS49"), is(false));
    }

    @Test
    public void transfer_in_authentication_error() {
        try {
            transferIn(AS50___far_last_of_nonripe_block, "nonExistingUser.wrongPassword,dummy topic");
        } catch(NotAuthorizedException exc) {
            assertThat(exc.getResponse().readEntity(String.class), containsString("FAILED_AUTHENTICATION") );
        }
    }

    // end of far last

    @Test
    public void transfer_in_iana_number() {
        try {
            transferIn(AS33___in_iana_block);
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("IANA blocks are not available for transfers: AS33"));
        }
    }

    @Test
    public void transfer_in_right_of_iana_block() {
        transferIn(AS41___nonripe_block_next_to_iana_block);

        // original deleted
        assertThat(objectExists("AS41 - AS50"), is(false));

        // iana untouched
        assertThat(objectExists("AS31 - AS40"), is(true));
        assertThat(isIanaAsBlock("AS31 - AS40"), is(true));

        // new created
        assertThat(objectExists("AS41 - AS41"), is(true));
        assertThat(isRipeAsBlock("AS41 - AS41"), is(true));

        // original shrunk
        assertThat(objectExists("AS42 - AS50"), is(true));
        assertThat(isNonRipeAsBlock("AS42 - AS50"), is(true));
    }

    @Test
    public void transfer_in_ripe_number() {
        assertThat(transferIn(AS11___in_ripe_block),
                containsString("Resource " + AS11___in_ripe_block + " is already RIPE"));
    }

    // error cases

    @Test
    public void transfer_in_non_existing_number() {
        try {
            transferIn(AS99___unknown_block);
            fail();
        } catch (NotFoundException e) {
            assertThat(getResponseBody(e), containsString("Block not found"));
        }
    }

    @Test
    public void transfer_in_syntax_error() {
        try {
            transferIn("AS*");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Invalid AS number: 'AS*'"));
        }
    }

    public void transfer_asn_from_unknown_region() {

        databaseHelper.addObject("" +
                "as-block:      AS241 - AS250\n" +
                "descr:         some unknown region\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        try {
            transferIn("AS241");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Block is not recognizable: AS241 - AS250"));
        }
    }

    @Test
    public void transfer_in_detect_left_neighbour_to_be_merged_first() {
        given:
        databaseHelper.addObject("" +
                "as-block:      AS51-AS60\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:      AS61-AS70\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        when:
        then:
        try {
            transferIn("AS60");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Adjacent block AS61-AS70 should be merged with current block AS51-AS60 first"));
        }
    }

    @Test
    public void transfer_in_allow_same_neighbour_when_no_merge_needed() {
        given:
        databaseHelper.addObject("" +
                "as-block:      AS51-AS60\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:      AS61-AS70\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        when:
        then:
        transferIn("AS62");
    }

    @Test
    public void transfer_in_detect_right_neighbour_to_be_merged_first() {
        given:
        databaseHelper.addObject("" +
                "as-block:      AS51-AS60\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:      AS61-AS70\n" +
                "descr:         " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        when:
        then:
        try {
            transferIn("AS61");
            fail();
        } catch (BadRequestException e) {
            assertThat(getResponseBody(e), containsString("Adjacent block AS51-AS60 should be merged with current block AS61-AS70 first"));
        }
    }

    // helper methods

    private String transferIn(final String asNum) {
        return transferIn(asNum, AbstractTransferTest.OVERRIDE_LINE);
    }

    private String transferIn(final String asNum, final String overrideLine) {

        ipTreeUpdater.updateTransactional();

        try {
            return RestTest.target(getPort(), "whois/transfer/aut-num/",
                    "override=" + SyncUpdateUtils.encode(overrideLine), null)
                    .path(URLEncoder.encode(asNum, "UTF-8"))
                    .request()
                    .post(Entity.text(null), String.class);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
            return null;
        }
    }

}
