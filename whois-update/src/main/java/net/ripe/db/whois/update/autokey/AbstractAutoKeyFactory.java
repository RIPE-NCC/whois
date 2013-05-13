package net.ripe.db.whois.update.autokey;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.autokey.dao.AutoKeyRepository;
import net.ripe.db.whois.update.domain.AutoKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

abstract class AbstractAutoKeyFactory<T extends AutoKey> implements AutoKeyFactory<T> {
    private static final Pattern AUTO_PATTERN = Pattern.compile("(?i)(AUTO-(?:\\d+))([A-Z]*)");
    private static final Splitter NAME_SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

    private final AutoKeyRepository<T> autoKeyRepository;

    protected AbstractAutoKeyFactory(final AutoKeyRepository<T> autoKeyRepository) {
        this.autoKeyRepository = autoKeyRepository;
    }

    private CIString source;

    @Value("${whois.source}")
    protected void setSource(final String source) {
        this.source = ciString(source);
    }

    CIString getSource() {
        return source;
    }

    @Override
    public boolean isApplicableFor(final RpslObject object) {
        return true;
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

    T generateForName(final CharSequence keyPlaceHolder, final String name) {
        Validate.notEmpty(name, "Name must not be empty");

        final Matcher matcher = AUTO_PATTERN.matcher(keyPlaceHolder);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid key request: " + keyPlaceHolder);
        }

        final String space = getSpace(matcher.group(2), name);
        return autoKeyRepository.claimNextAvailableIndex(space, getSource().toString());
    }

    private String getSpace(final String space, final String name) {
        if (StringUtils.isNotEmpty(space)) {
            return space;
        }

        final StringBuilder spaceBuilder = new StringBuilder();
        for (final String namePart : NAME_SPLITTER.split(name)) {
            final char c = namePart.charAt(0);
            if (Character.isLetter(c)) {
                spaceBuilder.append(Character.toUpperCase(c));

                if (spaceBuilder.length() == 4) {
                    break;
                }
            }
        }

        return StringUtils.rightPad(spaceBuilder.toString(), 2, 'A');
    }
}
