package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.update.domain.ReservedAutnum;
import static net.ripe.db.whois.common.rpsl.ObjectType.MNTNER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Component
public class RdapRequestValidator {

    private final RdapExceptionMapper rdapExceptionMapper;
    private final ReservedAutnum reservedAutnum;

    @Autowired
    public RdapRequestValidator(final RdapExceptionMapper rdapExceptionMapper, final ReservedAutnum reservedAutnum) {
        this.rdapExceptionMapper = rdapExceptionMapper;
        this.reservedAutnum = reservedAutnum;
    }

    public void validateDomain(final String key) {
        try {
            Domain.parse(key);
        } catch (AttributeParseException e) {
            throw rdapExceptionMapper.notFound("RIPE NCC does not support forward domain queries.");
        }
    }

    public void validateIp(final String rawUri, final String key) {
        try {
            IpInterval.parse(key);
        } catch (IllegalArgumentException e) {
            throw rdapExceptionMapper.badRequest("Invalid syntax.");
        }

        if (rawUri.contains("//")) {
            throw rdapExceptionMapper.badRequest("Invalid syntax.");
        }
    }

    public void validateAutnum(final String key) {
        try {
            AutNum.parse(key);
        } catch (AttributeParseException e) {
            throw rdapExceptionMapper.badRequest("Invalid syntax.");
        }
    }

    public void validateEntity(final String key) {
        if (key.toUpperCase().startsWith("ORG-")) {
            if (!AttributeType.ORGANISATION.isValidValue(ORGANISATION, key)) {
                throw rdapExceptionMapper.badRequest("Invalid syntax.");
            }
        } else {
            if (!AttributeType.MNTNER.isValidValue(MNTNER, key)) {
                throw rdapExceptionMapper.badRequest("Invalid syntax.");
            }
        }
    }

    public boolean isReservedAsNumber(String key) {
        return reservedAutnum.isReservedAsNumber( AutNum.parse(key).getValue());
    }
}