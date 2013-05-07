package net.ripe.db.whois.update.autokey;


import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.autokey.dao.X509Repository;
import net.ripe.db.whois.update.domain.X509KeycertId;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
public class X509AutoKeyFactory implements AutoKeyFactory<X509KeycertId> {
    private static final Pattern AUTO_PATTERN = Pattern.compile("(?i)(AUTO-\\d+)");
    private static final String SPACE = "X509";
    private final X509Repository repository;
    private final CIString source;

    @Autowired
    public X509AutoKeyFactory(final X509Repository repository, @Value("${whois.source}") String source) {
        this.repository = repository;
        this.source = ciString(source);
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.KEY_CERT;
    }

    @Override
    public boolean isKeyPlaceHolder(final CharSequence s) {
        return AUTO_PATTERN.matcher(s).matches();
    }

    @Override
    public CIString getKeyPlaceholder(final CharSequence s) {
        final Matcher matcher = AUTO_PATTERN.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid key placeholder: " + s);
        }

        return ciString(matcher.group(1));
    }

    @Override
    public X509KeycertId claim(final String key) throws ClaimException {
        throw new ClaimException(ValidationMessages.syntaxError(key, "must be AUTO-nnn for create"));
    }

    @Override
    public X509KeycertId generate(final String keyPlaceHolder, final RpslObject object) {
        Validate.notEmpty(object.getValueForAttribute(AttributeType.KEY_CERT).toString(), "Name must not be empty");

        final Matcher matcher = AUTO_PATTERN.matcher(keyPlaceHolder);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid key request: " + keyPlaceHolder);
        }

        return repository.claimNextAvailableIndex(SPACE, source.toString());
    }

    @Override
    public boolean isApplicableFor(final RpslObject object) {
        try {
            X509CertificateWrapper.parse(object);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
