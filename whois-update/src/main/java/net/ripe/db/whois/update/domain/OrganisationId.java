package net.ripe.db.whois.update.domain;

import org.apache.commons.lang.Validate;

public class OrganisationId extends AutoKey {

    public OrganisationId(final String space, final int index, final String suffix) {
        super(space, index, suffix);
        Validate.notNull(space, "space cannot be null");
        Validate.isTrue(index > 0, "index must be greater than 0");
        Validate.notNull(suffix, "suffix cannot be null");
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("ORG-")
                .append(getSpace().toUpperCase())
                .append(getIndex())
                .append("-")
                .append(getSuffix())
                .toString();
    }
}
