package net.ripe.db.whois.api.transfer;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.common.domain.User;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.ws.rs.WebApplicationException;

public abstract class AbstractTransferTest extends AbstractIntegrationTest {
    protected static final String OVERRIDE_USER = "overrideUser";
    protected static final String OVERRIDE_PASSWORD = "overridePassword";
    protected static final String OVERRIDE_LINE = OVERRIDE_USER + "," + OVERRIDE_PASSWORD + ",myreason";

    @Autowired
    protected RestClient restClient;

    @Before
    public void setUp() throws Exception {
        databaseHelper.insertUser(User.createWithPlainTextPassword(OVERRIDE_USER, OVERRIDE_PASSWORD, ObjectType.values()));

        databaseHelper.addObject("" +
                "person:        Any Anonymous\n" +
                "nic-hdl:       PERSON-TEST\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        RIPE-NCC-HM-MNT\n" +
                "descr:         Maintainer\n" +
                "admin-c:       PERSON-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        RIPE-NCC-HM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        RIPE-NCC-MNT\n" +
                "descr:         Maintainer\n" +
                "admin-c:       PERSON-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        RIPE-NCC-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        RIPE-DBM-MNT\n" +
                "descr:         Maintainer\n" +
                "admin-c:       PERSON-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        RIPE-DBM-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "person:        Any Anonymous\n" +
                "nic-hdl:       IANA1-RIPE\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        RIPE-NCC-RPSL-MNT\n" +
                "descr:         Maintainer\n" +
                "admin-c:       PERSON-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        RIPE-NCC-RPSL-MNT\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "organisation:    ORG-NCC1-RIPE\n" +
                "org-name:        RIPE Network Coordination Centre\n" +
                "org-type:        RIR\n" +
                "address:         RIPE NCC\n" +
                "address:         Singel 258\n" +
                "address:         1016AB\n" +
                "address:         Amsterdam\n" +
                "address:         NETHERLANDS\n" +
                "phone:           +31 20 535 4444\n" +
                "fax-no:          +31 20 535 4445\n" +
                "e-mail:          ncc@ripe.net\n" +
                "admin-c:         PERSON-TEST\n" +
                "tech-c:          PERSON-TEST\n" +
                "ref-nfy:         fake@ripe.net\n" +
                "mnt-ref:         RIPE-NCC-HM-MNT\n" +
                "notify:          fake@ripe.net\n" +
                "mnt-by:          RIPE-NCC-HM-MNT\n" +
                "created:         2004-04-17T09:55:47Z\n" +
                "last-modified:   2013-05-17T10:32:59Z\n" +
                "source:          TEST");

        databaseHelper.addObject("" +
                "organisation:   ORG-IANA1-RIPE\n" +
                "org-name:       Internet Assigned Numbers Authority\n" +
                "org-type:       IANA\n" +
                "address:        see http://www.iana.org\n" +
                "admin-c:        IANA1-RIPE\n" +
                "tech-c:         IANA1-RIPE\n" +
                "mnt-ref:        RIPE-NCC-HM-MNT\n" +
                "mnt-by:         RIPE-NCC-HM-MNT\n" +
                "created:        2004-04-17T09:57:29Z\n" +
                "last-modified:  2013-07-22T12:03:42Z\n" +
                "source:         TEST");
    }

    protected RpslObject lookup(final ObjectType objectType, final String primaryKey) {
        return databaseHelper.lookupObject(objectType, primaryKey);
    }

    protected boolean objectExists(final ObjectType objectType, final String primaryKey) {
        boolean status = true;
        try {
            lookup(objectType, primaryKey);
        } catch (EmptyResultDataAccessException e) {
            status = false;
        }
        return status;
    }

    protected String getResponseBody(final WebApplicationException e) {
        return e.getResponse().readEntity(String.class);
    }

}
