package net.ripe.db.whois.update.domain;

public class X509KeycertId extends AutoKey {


    public X509KeycertId(final String space, final int index, final String suffix) {
        super(space, index, suffix);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getSpace())
                .append("-")
                .append(getIndex())
                .toString();
    }
}
