package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import net.ripe.commons.ip.Asn;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Roa implements Comparator<Roa> {

    private final long asn;
    private final int maxLength;
    private final String prefix;
    private final TrustAnchor trustAnchor;

    @JsonCreator
    public Roa(final @JsonProperty("asn") String asn,
               final @JsonProperty("maxLength") int maxLength,
               final @JsonProperty("prefix") String prefix,
               final @JsonProperty("ta") String trustAnchor) {
        this(Asn.parse(asn).asBigInteger().longValue(), maxLength, prefix.toLowerCase(), TrustAnchor.fromRpkiName(trustAnchor));
    }

    public Roa(final long asn,
               final int maxLength,
               final String prefix,
               final TrustAnchor trustAnchor) {
        this.asn = asn;
        this.maxLength = maxLength;
        this.prefix = prefix;
        this.trustAnchor = trustAnchor;
    }

    public long getAsn() {
        return asn;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public String getPrefix() {
        return prefix;
    }

    public TrustAnchor getTrustAnchor() {
        return trustAnchor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(asn, maxLength, prefix, trustAnchor);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || (o.getClass() != getClass())) {
            return false;
        }

        return Objects.equals(asn, ((Roa)o).asn) &&
                Objects.equals(maxLength, ((Roa)o).maxLength) &&
                Objects.equals(prefix, ((Roa)o).prefix) &&
                Objects.equals(trustAnchor, ((Roa)o).trustAnchor);
    }

    @Override
    public int compare(final Roa left, final Roa right) {
        return new CompareToBuilder()
                .append(left.getPrefix(), right.getPrefix())
                .append(left.getMaxLength(), right.getMaxLength())
                .append(left.getAsn(), right.getAsn())
                .append(left.getTrustAnchor(), right.getTrustAnchor())
                .toComparison();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Roa.class)
                .add("asn", asn)
                .add("maxLength", maxLength)
                .add("prefix", prefix)
                .add("trustAnchor", trustAnchor)
                .toString();
    }

}
