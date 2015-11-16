package net.ripe.db.whois.scheduler.task.loader;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.autokey.ClaimException;
import net.ripe.db.whois.update.autokey.NicHandleFactory;
import net.ripe.db.whois.update.autokey.dao.OrganisationIdRepository;
import net.ripe.db.whois.update.autokey.dao.X509Repository;
import net.ripe.db.whois.update.domain.OrganisationId;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.X509KeycertId;
import net.ripe.db.whois.update.handler.UpdateAbortedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ObjectLoader {
    private static final Pattern X509_PATTERN = Pattern.compile("(?i)^X509-([1-9][0-9]+)$");
    private static final Pattern ORGID_PATTERN = Pattern.compile("(?i)^ORG-([A-Z]{2,4})([0-9]+)-([A-Z0-9_-]+)$");

    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final AttributeSanitizer attributeSanitizer;
    final NicHandleFactory nicHandleFactory;
    final OrganisationIdRepository organisationIdRepository;
    final X509Repository x509Repository;

    @Autowired
    public ObjectLoader(RpslObjectDao rpslObjectDao,
                        RpslObjectUpdateDao rpslObjectUpdateDao,
                        AttributeSanitizer attributeSanitizer,
                        NicHandleFactory nicHandleFactory,
                        OrganisationIdRepository organisationIdRepository,
                        X509Repository x509Repository) {
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.attributeSanitizer = attributeSanitizer;
        this.nicHandleFactory = nicHandleFactory;
        this.organisationIdRepository = organisationIdRepository;
        this.x509Repository = x509Repository;
    }

    public void processObject(final String fullObject,
                              final Result result,
                              final int pass,
                              final LoaderMode loaderMode) {

        RpslObject rpslObject = RpslObject.parse(fullObject);
        rpslObject = attributeSanitizer.sanitize(rpslObject, new ObjectMessages());

        if (loaderMode == LoaderMode.FAST_AND_RISKY) {
            addObjectRisky(rpslObject, result, pass);
        }

        addObjectSafe(rpslObject, result, pass);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    private void addObjectSafe(RpslObject rpslObject, Result result, int pass) {
        try {
            addObject(rpslObject, result, pass);
        } catch (Exception e) {
            printExceptionToResult(e, result, pass, rpslObject.getFormattedKey());
            throw new UpdateAbortedException();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void addObjectRisky(RpslObject rpslObject, Result result, int pass) {
        try {
            addObject(rpslObject, result, pass);
        } catch (Exception e) {
            printExceptionToResult(e, result, pass, rpslObject.getFormattedKey());
        }
    }

    private void printExceptionToResult(final Exception e, final Result result, final int pass, final String formattedKey) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        result.addFail(String.format("Error in pass %d in '%s': %s\n", pass, formattedKey, stringWriter), pass);
    }

    private void addObject(RpslObject rpslObject, Result result, int pass) throws Exception {
        if (pass == 1) {
            checkForReservedNicHandle(rpslObject);
            rpslObject = RpslObjectFilter.keepKeyAttributesOnly(new RpslObjectBuilder(rpslObject)).get();
            rpslObjectUpdateDao.createObject(rpslObject);
        } else {
            final RpslObjectInfo existing = rpslObjectDao.findByKey(rpslObject.getType(), rpslObject.getKey().toString());
            rpslObjectUpdateDao.updateObject(existing.getObjectId(), rpslObject);
            claimIds(rpslObject);
            result.addSuccess();
        }
    }

    public void checkForReservedNicHandle(final RpslObject object) throws ClaimException {
        if (object.getType() != ObjectType.PERSON && object.getType() != ObjectType.ROLE) {
            return;
        }

        final CIString pkey = object.getKey();

        if (pkey.equals("AUTO-1")){
            throw new ClaimException(new Message(Messages.Type.ERROR, "AUTO-1 in nic-hdl is not available in Bootstrap/LoadDump mode"));
        }

        if (!nicHandleFactory.isAvailable(object.getKey().toString())){
            throw new ClaimException(UpdateMessages.nicHandleNotAvailable(object.getKey()));
        }

    }

    // NB: `synchronized` is mandatory here as normally IDs are claimed under the hood of the global update lock
    public void claimIds(RpslObject rpslObject) throws Exception {
        final String key = rpslObject.getKey().toString();

        switch (rpslObject.getType()) {
            case PERSON:
            case ROLE:
                synchronized (nicHandleFactory) {
                    nicHandleFactory.claim(key);
                }
                break;

            case ORGANISATION:
                // organisations would normally take AUTO-* keys only and generate a name, so we have to implement a manual claiming here
                synchronized (organisationIdRepository) {
                    Matcher orgMatcher = ORGID_PATTERN.matcher(key);
                    if (!orgMatcher.matches()) {
                        throw new IllegalArgumentException("Organisation key '" + key + "' is invalid");
                    }

                    final String space = orgMatcher.group(1);
                    final int index = orgMatcher.group(2).length() > 0 ? Integer.parseInt(orgMatcher.group(2)) : 0;
                    final String suffix = orgMatcher.group(3);

                    OrganisationId organisationId = new OrganisationId(space, index, suffix);
                    organisationIdRepository.claimSpecified(organisationId);
                }
                break;

            case KEY_CERT:
                synchronized (x509Repository) {
                    Matcher keycertMatcher = X509_PATTERN.matcher(key);
                    if (!keycertMatcher.matches()) {
                        break;  // could be pgp
                    }
                    final X509KeycertId autoKey = new X509KeycertId(null, Integer.parseInt(keycertMatcher.group(1)), null);
                    x509Repository.claimSpecified(autoKey);
                }
                break;
        }
    }
}
