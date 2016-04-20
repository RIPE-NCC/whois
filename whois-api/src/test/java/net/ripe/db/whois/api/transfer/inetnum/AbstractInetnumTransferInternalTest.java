package net.ripe.db.whois.api.transfer.inetnum;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.syncupdate.SyncUpdateUtils;
import net.ripe.db.whois.api.transfer.AbstractTransferTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.Assert.fail;

public abstract class AbstractInetnumTransferInternalTest extends AbstractTransferTest {

    @Before
    public void before() {
        databaseHelper.addObject("" +
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        EU # Country field is actually all countries in the world and not just EU countries\n" +
                "org:            ORG-IANA1-RIPE\n" +
                "admin-c:        IANA1-RIPE\n" +
                "tech-c:         IANA1-RIPE\n" +
                "status:         ALLOCATED UNSPECIFIED\n" +
                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                "source:         TEST");
    }

    protected boolean inetNumExists(final String primaryKey) {
        ipTreeUpdater.rebuild();

        try {
            RpslObject found = lookup(ObjectType.INETNUM, primaryKey);
            return found != null;
        } catch (EmptyResultDataAccessException exc) {
            return false;
        }
    }

    protected boolean inetnumWithNetnameExists(final String primaryKey, final String netname) {
        ipTreeUpdater.rebuild();

        try {
            RpslObject found = lookup(ObjectType.INETNUM, primaryKey);
            return netname.equals(found.findAttribute(AttributeType.NETNAME).getValue().trim());
        } catch (EmptyResultDataAccessException exc) {
            return false;
        }
    }

    protected boolean isMaintainedInRirSpace(final String resource) {
        return internalsTemplate.queryForObject("SELECT count(*) FROM authoritative_resource WHERE source = ? AND resource = ?", Integer.class, "test", resource) > 0;
    }

    protected String printErrorMessage(WhoisResources whoisResources) {
        for (ErrorMessage msg : whoisResources.getErrorMessages()) {
            System.err.println(msg.getSeverity() + ":" + msg);
        }
        return null;
    }


    protected String getInfoMessage(WhoisResources whoisResources) {
        for (ErrorMessage msg : whoisResources.getErrorMessages()) {
            if (msg.getSeverity().equals("Info")) {
                return msg.toString();
            }
        }
        return null;
    }

    protected String getErrorMessage(WhoisResources whoisResources) {
        for (ErrorMessage msg : whoisResources.getErrorMessages()) {
            if (msg.getSeverity().equals("Error")) {
                return msg.toString();
            }
        }
        return null;
    }
}
