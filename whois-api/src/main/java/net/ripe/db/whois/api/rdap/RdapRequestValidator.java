package net.ripe.db.whois.api.rdap;

import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.common.rpsl.attrs.AutNum;
import net.ripe.db.whois.common.rpsl.attrs.Domain;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.planner.AbuseCFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static net.ripe.db.whois.common.rpsl.ObjectType.ORGANISATION;

@Component
public class RdapRequestValidator {

    private RdapExceptionMapper rdapExceptionMapper;

    @Autowired
    public RdapRequestValidator(final RdapExceptionMapper rdapExceptionMapper) {
        this.rdapExceptionMapper = rdapExceptionMapper;
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
                throw new IllegalArgumentException("Invalid syntax.");
            }
        } else {
            if (!AttributeType.NIC_HDL.isValidValue(ObjectType.PERSON, key)) {
                throw new IllegalArgumentException("Invalid syntax.");
            }
        }
    }
}