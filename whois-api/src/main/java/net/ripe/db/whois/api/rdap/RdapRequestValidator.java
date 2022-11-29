package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.domain.ReservedResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Component
public class RdapRequestValidator {

    private final ReservedResources reservedResources;

    @Autowired
    public RdapRequestValidator(final ReservedResources reservedResources) {
        this.reservedResources = reservedResources;
    }

    public void validateDomain(final String key) {
        if (isEmpty(key)) {
            throw new BadRequestException("empty lookup term");
        }

        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw new NotFoundException("RIPE NCC does not support forward domain queries.");
        }
    }

    public void validateIp(final String rawUri, final String key) {
        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid syntax.");
        }

        if (rawUri.contains("//")) {
            throw new BadRequestException("Invalid syntax.");
        }
    }

    public void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw new BadRequestException("Invalid syntax.");
        }
    }

    public void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw new NotFoundException("Invalid syntax.");
            }
        } else {
            if (!AttributeType.MNTNER.isValidValue(MNTNER, key)) {
                throw new NotFoundException("Invalid syntax.");
            }
        }
    }

    public boolean isReservedAsNumber(String key) {
        return reservedResources.isReservedAsNumber( AutNum.parse(key).getValue());
    }
}