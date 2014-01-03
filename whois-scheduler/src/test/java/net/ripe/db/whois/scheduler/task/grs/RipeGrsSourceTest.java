package net.ripe.db.whois.scheduler.task.grs;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.source.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RipeGrsSourceTest {
    @Mock SourceContext sourceContext;
    @Mock DateTimeProvider dateTimeProvider;
    @Mock AuthoritativeResourceData authoritativeResourceData;
    @Mock Downloader downloader;

    RipeGrsSource subject;
    CaptureInputObjectHandler objectHandler;

    @Before
    public void setUp() throws Exception {
        objectHandler = new CaptureInputObjectHandler();
        subject = new RipeGrsSource("RIPE-GRS", sourceContext, dateTimeProvider, authoritativeResourceData, downloader, "");
    }

    @Test
    public void handleObjects() throws Exception {
        final File file = new File(getClass().getResource("/grs/ripe.test.gz").toURI());

        subject.handleObjects(file, objectHandler);

        assertThat(objectHandler.getObjects(), hasSize(0));
        assertThat(objectHandler.getLines(), hasSize(2));
        assertThat(objectHandler.getLines(), contains((List<String>)
                Lists.newArrayList(
                        "as-block:        AS1877 - AS1901\n",
                        "descr:           RIPE NCC ASN block\n",
                        "remarks:         These AS Numbers are further assigned to network\n",
                        "remarks:         operators in the RIPE NCC service region. AS\n",
                        "remarks:         assignment policy is documented in:\n",
                        "remarks:         <http://www.ripe.net/ripe/docs/asn-assignment.html>\n",
                        "remarks:         RIPE NCC members can request AS Numbers using the\n",
                        "remarks:         form available in the LIR Portal or at:\n",
                        "remarks:         <http://www.ripe.net/ripe/docs/asnrequestform.html>\n",
                        "org:             ORG-NCC1-RIPE\n",
                        "admin-c:         DUMY-RIPE\n",
                        "tech-c:          DUMY-RIPE\n",
                        "mnt-by:          RIPE-DBM-MNT\n",
                        "mnt-lower:       RIPE-NCC-HM-MNT\n",
                        "changed:         hostmaster@ripe.net 20090529\n",
                        "source:          RIPE\n",
                        "remarks:         ****************************\n",
                        "remarks:         * THIS OBJECT IS MODIFIED\n",
                        "remarks:         * Please note that all data that is generally regarded as personal\n",
                        "remarks:         * data has been removed from this object.\n",
                        "remarks:         * To view the original object, please query the RIPE Database at:\n",
                        "remarks:         * http://www.ripe.net/whois\n",
                        "remarks:         ****************************\n"),

                Lists.newArrayList(
                        "as-block:        AS2043 - AS2043\n",
                        "descr:           RIPE NCC ASN block\n",
                        "remarks:         These AS Numbers are further assigned to network\n",
                        "remarks:         operators in the RIPE NCC service region. AS\n",
                        "remarks:         assignment policy is documented in:\n",
                        "remarks:         <http://www.ripe.net/ripe/docs/asn-assignment.html>\n",
                        "remarks:         RIPE NCC members can request AS Numbers using the\n",
                        "remarks:         form available in the LIR Portal or at:\n",
                        "remarks:         <http://www.ripe.net/ripe/docs/asnrequestform.html>\n",
                        "org:             ORG-NCC1-RIPE\n",
                        "admin-c:         DUMY-RIPE\n",
                        "tech-c:          DUMY-RIPE\n",
                        "mnt-by:          RIPE-DBM-MNT\n",
                        "mnt-lower:       RIPE-NCC-HM-MNT\n",
                        "changed:         hostmaster@ripe.net 20090529\n",
                        "source:          RIPE\n",
                        "remarks:         ****************************\n",
                        "remarks:         * THIS OBJECT IS MODIFIED\n",
                        "remarks:         * Please note that all data that is generally regarded as personal\n",
                        "remarks:         * data has been removed from this object.\n",
                        "remarks:         * To view the original object, please query the RIPE Database at:\n",
                        "remarks:         * http://www.ripe.net/whois\n",
                        "remarks:         ****************************\n")
        ));
    }
}
