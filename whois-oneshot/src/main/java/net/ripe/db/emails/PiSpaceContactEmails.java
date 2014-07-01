package net.ripe.db.emails;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 *  Find contact email addresses for PI space resources.
 *
 *  PI space is defined as inetnum / inet6num objects with the status "ASSIGNED PI".
 *
 *  First log the distinct e-mail attributes of all tech-c attributes of these resources.
 *
 *  Then log the distinct upd-to attributes of the maintainers of the resource organisations.
 */
public class PiSpaceContactEmails {

    private static final Logger LOGGER = LoggerFactory.getLogger(PiSpaceContactEmails.class);

    public static void main(final String[] args) {

        final Set<CIString> techCs = findResourceTechCs();
        logTechCEmails(techCs);

        final Set<RpslObject> maintainers = findResourceOrganisationMaintainers();
        logUpdTo(maintainers);
    }

    private static Map<String, RpslObject> loadObjects(final String resource) {
        final Map<String, RpslObject> rpslObjects = Maps.newHashMap();
        for (String rpslString : new RpslObjectFileReader(Resources.getResource(resource).getFile())) {
            final RpslObject rpslObject = RpslObject.parse(rpslString);
            rpslObjects.put(rpslObject.getKey().toString(), rpslObject);
        }

        return rpslObjects;
    }

    // find resource tech-c's

    private static Set<CIString> findResourceTechCs() {
        final Set<CIString> result = Sets.newHashSet();
        final CIString assignedPi = CIString.ciString(InetnumStatus.ASSIGNED_PI.toString());

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.inetnum.gz").getFile())) {
            final RpslObject inetnum = RpslObject.parse(rpslString);
            if (assignedPi.equals(inetnum.getValueOrNullForAttribute(AttributeType.STATUS))) {
                final CIString techc = inetnum.getValueOrNullForAttribute(AttributeType.TECH_C);
                if (techc != null) {
                    result.add(techc);
                }
            }
        }

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.inet6num.gz").getFile())) {
            final RpslObject inet6num = RpslObject.parse(rpslString);
            if (assignedPi.equals(inet6num.getValueOrNullForAttribute(AttributeType.STATUS))) {
                final CIString techc = inet6num.getValueOrNullForAttribute(AttributeType.TECH_C);
                if (techc != null) {
                    result.add(techc);
                }
            }
        }

        return result;
    }

    // log tech-c emails

    private static void logTechCEmails(final Set<CIString> techCs) {
        final Set<CIString> emails = Sets.newHashSet();

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.person.gz").getFile())) {
            final RpslObject person = RpslObject.parse(rpslString);
            if (techCs.contains(person.getKey())) {
                for (CIString email : person.getValuesForAttribute(AttributeType.E_MAIL)) {
                    emails.add(email);
                }
                techCs.remove(person.getKey());
            }
        }

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.role.gz").getFile())) {
            final RpslObject role = RpslObject.parse(rpslString);
            if (techCs.contains(role.getKey())) {
                for (CIString email : role.getValuesForAttribute(AttributeType.E_MAIL)) {
                    emails.add(email);
                }
                techCs.remove(role.getKey());
            }
        }

        for (CIString email : emails) {
            LOGGER.info(email.toString());
        }
    }

    // find resource -> organisation -> maintainers

    private static Set<RpslObject> findResourceOrganisationMaintainers() {

        final Set<RpslObject> result = Sets.newHashSet();

        final Map<String, RpslObject> maintainers = loadObjects("ripe.db.mntner.gz");
        final Map<String, RpslObject> organisations = loadObjects("ripe.db.organisation.gz");

        final CIString assignedPi = CIString.ciString(InetnumStatus.ASSIGNED_PI.toString());

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.inetnum.gz").getFile())) {
            final RpslObject inetnum = RpslObject.parse(rpslString);
            if (assignedPi.equals(inetnum.getValueOrNullForAttribute(AttributeType.STATUS))) {
                final CIString orgName = inetnum.getValueOrNullForAttribute(AttributeType.ORG);
                if (orgName != null) {
                    final RpslObject orgObject = organisations.get(orgName.toString());
                    if (orgObject != null) {
                        for (CIString mntnerName : orgObject.getValuesForAttribute(AttributeType.MNT_BY)) {
                            final RpslObject mntnerObject = maintainers.get(mntnerName.toString());
                            if (mntnerObject != null) {
                                result.add(mntnerObject);
                            }
                        }
                    }
                }
            }
        }

        for (String rpslString : new RpslObjectFileReader(Resources.getResource("ripe.db.inet6num.gz").getFile())) {
            final RpslObject inet6num = RpslObject.parse(rpslString);
            if (assignedPi.equals(inet6num.getValueOrNullForAttribute(AttributeType.STATUS))) {
                final CIString orgName = inet6num.getValueOrNullForAttribute(AttributeType.ORG);
                if (orgName != null) {
                    final RpslObject orgObject = organisations.get(orgName.toString());
                    if (orgObject != null) {
                        for (CIString mntnerName : orgObject.getValuesForAttribute(AttributeType.MNT_BY)) {
                            final RpslObject mntnerObject = maintainers.get(mntnerName.toString());
                            if (mntnerObject != null) {
                                result.add(mntnerObject);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    // log maintainers upd-to

    private static void logUpdTo(final Set<RpslObject> maintainers) {
        final Set<CIString> emails = Sets.newHashSet();

        for (RpslObject maintainer : maintainers) {
            for (CIString updTo : maintainer.getValuesForAttribute(AttributeType.UPD_TO)) {
                emails.add(updTo);
            }
        }

        for (CIString updTo : emails) {
            LOGGER.info(updTo.toString());
        }
    }
}
