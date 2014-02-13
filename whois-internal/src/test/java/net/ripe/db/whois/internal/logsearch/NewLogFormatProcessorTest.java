package net.ripe.db.whois.internal.logsearch;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NewLogFormatProcessorTest {

    @Test
    public void indexed_msg_log_entries_regex() {
        assertTrue(NewLogFormatProcessor.INDEXED_MSG_LOG_ENTRIES.matcher("/var/tmp/235206.syncupdate_193.2.3.243_4691349034341497/001.msg-in.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_MSG_LOG_ENTRIES.matcher("/var/tmp/235206.syncupdate_193.2.3.243_4691349034341497/002.msg-out.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_MSG_LOG_ENTRIES.matcher("/var/tmp/20140201.tar/100102.0.10820740847182064/001.msg-in.txt.gz").matches());
        assertTrue(NewLogFormatProcessor.INDEXED_MSG_LOG_ENTRIES.matcher("/var/tmp/233302.E1W11gL-00057U-Rr/002.msg-out.txt.gz").matches());
        assertFalse(NewLogFormatProcessor.INDEXED_MSG_LOG_ENTRIES.matcher("/var/tmp/233302.E1W11gL-00057U-Rr/001.ack.txt.gz").matches());
    }

    @Test
    public void indexed_ack_log_entries_regex() {
        assertTrue(NewLogFormatProcessor.INDEXED_ACK_LOG_ENTRIES.matcher("/var/tmp/220546.rest_193.2.3.44_4684969805788132/001.ack.txt.gz").matches());
        assertFalse(NewLogFormatProcessor.INDEXED_ACK_LOG_ENTRIES.matcher("/var/tmp/235206.syncupdate_193.2.3.243_4691349034341497/001.ack.txt.gz").matches());
        assertFalse(NewLogFormatProcessor.INDEXED_ACK_LOG_ENTRIES.matcher("/var/tmp/233302.E1W11gL-00057U-Rr/001.ack.txt.gz").matches());
    }


}
