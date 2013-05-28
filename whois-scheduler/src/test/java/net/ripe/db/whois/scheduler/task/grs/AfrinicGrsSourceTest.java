package net.ripe.db.whois.scheduler.task.grs;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AfrinicGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock Downloader downloader;

    CaptureInputObjectHandler objectHandler;
    AfrinicGrsSource subject;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new AfrinicGrsSource("AFRINIC-GRS", sourceContext, dateTimeProvider, authoritativeResourceData, downloader, "");
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/afrinic.test.bz2").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getObjects(), hasSize(0));
        assertThat(objectHandler.getLines(), hasSize(5));
        assertThat(objectHandler.getLines().get(0), contains(
                "as-block:     AS30980 - AS30980\n",
                "descr:        AfriNIC ASN block\n",
                "remarks:      These AS Numbers are further assigned to network\n",
                "              operators in the AfriNIC service region. AS\n",
                "              assignment policy is documented in:\n",
                "              <http://www.afrinic.net/policies/afpol-as200407-000.htm>\n",
                "              AfriNIC members can request AS Numbers using the\n",
                "              form located at:\n",
                "              http://www.afrinic.net/documents.htm\n",
                "org:          ORG-AFNC1-AFRINIC\n",
                "admin-c:      TEAM-AFRINIC\n",
                "tech-c:       TEAM-AFRINIC\n",
                "mnt-by:       AFRINIC-HM-MNT\n",
                "mnt-lower:    AFRINIC-HM-MNT\n",
                "changed:      hostmaster@afrinic.net 20050101\n",
                "changed:      hostmaster@afrinic.net 20050205\n",
                "remarks:      data has been transferred from RIPE Whois Database 20050221\n",
                "source:       AFRINIC\n"));

        assertThat(objectHandler.getLines().get(1), hasItem("inetnum:      196.207.3.172 - 196.207.3.175\n"));
        assertThat(objectHandler.getLines().get(2), hasItem("inetnum:      196.204.208.1 - 196.204.208.255\n"));
        assertThat(objectHandler.getLines().get(3), hasItem("as-block:     AS30720 - AS30979\n"));
        assertThat(objectHandler.getLines().get(4), hasItem("as-block:     AS31000 - AS31743\n"));
    }
}
