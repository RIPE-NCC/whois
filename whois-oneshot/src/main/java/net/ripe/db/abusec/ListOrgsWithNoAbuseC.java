package net.ripe.db.abusec;

import com.google.common.collect.ImmutableList;
import net.ripe.db.LogUtil;
import net.ripe.db.whois.api.rest.RestClient;
import net.ripe.db.whois.common.domain.io.Downloader;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListOrgsWithNoAbuseC {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListOrgsWithNoAbuseC.class);

    private static final RestClient restClient = new RestClient("https://rest.db.ripe.net", "RIPE");
    private static final Downloader downloader = new Downloader();

    private static final List<String> splitFiles = ImmutableList.of(
            "/ncc/ftp/ripe/dbase/split/ripe.db.inetnum.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.inet6num.gz",
            "/ncc/ftp/ripe/dbase/split/ripe.db.aut-num.gz");


    static void checkOrg(String orgId) {

    }

    public static void main(final String[] argv) throws Exception {
        LogUtil.initLogger();

        for (String splitFile : splitFiles) {
            for (String nextObject : new RpslObjectFileReader(splitFile)) {
                RpslObject rpslObject;
                try {
                    rpslObject = RpslObject.parse(nextObject);
                } catch (Exception e) {
                    continue;
                }

                List<RpslAttribute> org = rpslObject.findAttributes(AttributeType.ORG);

                switch (rpslObject.getType()) {
                    case INET6NUM:
                        try {
                            if (Inet6numStatus.getStatusFor(rpslObject.getValueForAttribute(AttributeType.STATUS)) == Inet6numStatus.ASSIGNED_PI) {

                            }
                        } catch (RuntimeException e) {

                        }
                        break;
                    case INETNUM:
                        break;
                    case AUT_NUM:
                        break;
                    default:
                        LOGGER.error("Ignoring object " + rpslObject.getFormattedKey());
                        continue;
                }


            }
        }
    }
}
