package net.ripe.db.whois.common.domain;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class RpslObjectVersionsTest {
    @Test
    public void parse_versions() throws Exception {

        List<RpslObjectVersions.Entry> entries = RpslObjectVersions.parse("% This is the RIPE Database query service.\n" +
                "% The objects are in RPSL format.\n" +
                "%\n" +
                "% The RIPE Database is subject to Terms and Conditions.\n" +
                "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "\n" +
                "% Version history for object  193.0.0.0 - 193.0.7.255\n" +
                "% You can use -Zrev# to get an exact version of the object.\n" +
                "\n" +
                "rev#\tDate\t\t\t\tOp.\t\t\n" +
                "1\t2003-03-17 12:40\tADD/UPD\t\t\n" +
                "2\t2003-03-17 12:41\tADD/UPD\t\t\n" +
                "3\t2003-03-17 12:41\tDEL\t\t\n" +
                "4\t2003-03-17 13:15\tADD/UPD\t\t\n" +
                "5\t2003-04-12 08:32\tADD/UPD\t\t\n" +
                "6\t2003-05-22 13:20\tADD/UPD\t\t\n" +
                "7\t2004-10-22 14:43\tADD/UPD\t\t\n" +
                "8\t2004-10-31 03:08\tADD/UPD\t\t\n" +
                "9\t2006-02-21 16:46\tADD/UPD\t\t\n" +
                "10\t2009-12-02 13:27\tADD/UPD\t\t\n" +
                "11\t2009-12-02 13:49\tADD/UPD\t\t\n" +
                "12\t2010-03-17 15:00\tADD/UPD\t\t\n" +
                "13\t2011-02-17 12:11\tADD/UPD\t\t\n").getVersions();

        assertTrue(entries.size() == 13);

        int i = 1;
        for (RpslObjectVersions.Entry entry : entries) {
            assertEquals(entry.getVersion(), i);
            if (i == 3) {
                assertEquals(entry.getOperation(), RpslObjectVersions.Operation.DELETE);
            } else {
                assertEquals(entry.getOperation(), RpslObjectVersions.Operation.ADD_UPDATE);
            }

            i++;
        }
    }

}
