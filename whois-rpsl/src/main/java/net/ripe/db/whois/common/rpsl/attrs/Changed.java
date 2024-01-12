package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.CheckForNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Changed {
    private static final Pattern CHANGED_PATTERN = Pattern.compile("^([^ ]+@(?:[^. ]+[.])+[^. ]+)(?:[ ]([0-9]{8}))?$");
    private static final DateTimeFormatter CHANGED_ATTRIBUTE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_LENGTH = 89;

    private final String email;
    private final LocalDate date;

    public Changed(final CIString email, final LocalDate date) {
        this(email.toString(), date);
    }

    public Changed(final String email, final LocalDate date) {
        this.email = email;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    @CheckForNull
    public LocalDate getDate() {
        return date;
    }

    @CheckForNull
    public String getDateString() {
        if (date == null) {
            return null;
        }

        return CHANGED_ATTRIBUTE_DATE_FORMAT.format(date);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();

        result.append(email);

        final String dateString = getDateString();
        if (dateString != null) {
            result.append(" ").append(dateString);
        }

        return result.toString();
    }

    public static Changed parse(final CIString value) {
        return parse(value.toString());
    }

    public static Changed parse(final String value) {
        if (value.length() > MAX_LENGTH) {
            throw new AttributeParseException("Too long", value);
        }

        final Matcher matcher = CHANGED_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final String email = matcher.group(1).trim();
        final String dateString = matcher.group(2);
        final LocalDate date;

        if (dateString == null) {
            date = null;
        } else {
            try {
                date = LocalDate.from(CHANGED_ATTRIBUTE_DATE_FORMAT.parse(dateString));
            } catch (DateTimeParseException e) {
                throw new AttributeParseException("Invalid date: " + dateString, value);
            }
        }

        return new Changed(email, date);
    }
}
