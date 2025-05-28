package net.ripe.db.whois.update.handler.transform;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class MntRoutesAttributeTransformerTest {

    @Mock
    private Update update;
    @Mock
    private UpdateContext updateContext;

    private MntRoutesAttributeTransformer subject;

    @BeforeEach
    public void setUp() throws Exception {
        this.subject = new MntRoutesAttributeTransformer();
    }

    @Test
    public void filter_mnt_routes_from_autnum() {
        final RpslObject autnum = RpslObject.parse(
            "aut-num:        AS3333\n" +
            "as-name:        TEST-AS\n" +
            "org:            ORG-TEST1-TEST\n" +
            "                # Transit:\n" +
            "import:           from AS3333 action pref=700; accept ANY   #   preserve \n" +
            "export:           to AS3333 announce AS3333:AS-TEST         # formatting  \n" +
            "admin-c:        AA1-TEST\n" +
            "tech-c:         AA1-TEST\n" +
            "status:         ASSIGNED\n" +
            "mnt-by:         RIPE-NCC-END-MNT\n" +
            "mnt-by:         OWNER-MNT\n" +
            "mnt-routes:     ANOTHER-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2018-7-25T09:01:00Z\n" +
            "source:         TEST");

        final RpslObject updatedAutnum = subject.transform(autnum, update, updateContext, Action.MODIFY);

        assertThat(updatedAutnum.containsAttribute(AttributeType.MNT_ROUTES), is(false));
        assertThat(diff(autnum, updatedAutnum), is(
            "@@ -11,3 +11,2 @@\n" +
             " mnt-by:         OWNER-MNT\n" +
             "-mnt-routes:     ANOTHER-MNT\n" +
             " created:        1970-01-01T00:00:00Z\n"));
        verifyMessageAdded();
    }

    @Test
    public void no_mnt_routes_in_autnum() {
        final RpslObject autnum = RpslObject.parse(
            "aut-num:        AS3333\n" +
            "as-name:        TEST-AS\n" +
            "org:            ORG-TEST1-TEST\n" +
            "                # Transit:\n" +
            "import:         from AS3333 action pref=700; accept ANY\n" +
            "export:         to AS3333 announce AS3333:AS-TEST\n" +
            "admin-c:        AA1-TEST\n" +
            "tech-c:         AA1-TEST\n" +
            "status:         ASSIGNED\n" +
            "mnt-by:         RIPE-NCC-END-MNT\n" +
            "mnt-by:         OWNER-MNT\n" +
            "created:        1970-01-01T00:00:00Z\n" +
            "last-modified:  2018-7-25T09:01:00Z\n" +
            "source:         TEST");

        final RpslObject updatedAutnum = subject.transform(autnum, update, updateContext, Action.MODIFY);

        assertThat(updatedAutnum.containsAttribute(AttributeType.MNT_ROUTES), is(false));
        assertThat(diff(autnum, updatedAutnum), is(emptyString()));
        verifyMessageNotAdded();
    }

    @Test
    public void dont_filter_mnt_routes_from_inetnum() {
        final RpslObject inetnum = RpslObject.parse(
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "org:          ORG-LIR1-TEST\n" +
                "admin-c:      TP1-TEST\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED UNSPECIFIED\n" +
                "mnt-by:       RIPE-NCC-HM-MNT\n" +
                "mnt-lower:    LIR-MNT\n" +
                "mnt-routes:   LIR-MNT\n" +
                "source:       TEST\n");

        final RpslObject updatedInetnum = subject.transform(inetnum, update, updateContext, Action.MODIFY);

        assertThat(updatedInetnum.containsAttribute(AttributeType.MNT_ROUTES), is(true));
        assertThat(diff(inetnum, updatedInetnum), is(emptyString()));
        verifyMessageNotAdded();
    }

    // helper methods

    private String diff(final RpslObject original, final RpslObject updated) {
        return RpslObjectFilter.diff(original, updated);
    }

    private void verifyMessageAdded() {
        verify(updateContext).addMessage(update, UpdateMessages.mntRoutesAttributeRemoved());
        verifyNoMoreInteractions(updateContext);
    }

    private void verifyMessageNotAdded() {
        verifyNoMoreInteractions(updateContext);
    }

}
