package net.ripe.db.whois.rdap;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class RdapRequestValidator {

    private final ReservedResources reservedResources;

    @Autowired
    public RdapRequestValidator(final ReservedResources reservedResources) {
        this.reservedResources = reservedResources;
    }

    public void validateDomain(final String key) {
        if (isEmpty(key)) {
            throw new RdapException("Bad Request", "empty lookup term", HttpStatus.BAD_REQUEST_400);
        }

        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw new RdapException("Bad Request", "RIPE NCC does not support forward domain queries.",
                    HttpStatus.BAD_REQUEST_400);
        }
    }

    public void validateIp(final String rawUri, final String key) {
        if (isEmpty(key)) {
            throw new RdapException("Bad Request", "empty lookup term", HttpStatus.BAD_REQUEST_400);
        }

        if (rawUri.contains("//")) {
            throw new RdapException("Bad Request", "Ambiguous URI empty segment", HttpStatus.BAD_REQUEST_400);
        }

        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw new RdapException("Bad Request", e.getMessage(), HttpStatus.BAD_REQUEST_400);
        }
    }

    public void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw new RdapException("Bad Request", e.getMessage(), HttpStatus.BAD_REQUEST_400);
        }
    }

    public void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw new RdapException("Bad Request", "Bad organisation or mntner syntax: " + key,
                        HttpStatus.BAD_REQUEST_400);
            }
        } else {
            if (!AttributeType.MNTNER.isValidValue(MNTNER, key)) {
                throw new RdapException("Bad Request", "Bad organisation or mntner syntax: " + key,
                        HttpStatus.BAD_REQUEST_400);
            }
        }
    }

    public boolean isReservedAsNumber(String key) {
        return reservedResources.isReservedAsNumber( AutNum.parse(key).getValue());
    }
}
