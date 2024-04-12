package net.ripe.db.whois.common.rpki;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.ripe.commons.ip.Asn;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Roa {

    private final long asn;
    private final int maxLength;
    private final String prefix;
    private final TrustAnchor trustAnchor;

    @JsonCreator
    public Roa(final @JsonProperty("asn") String asn,
               final @JsonProperty("maxLength") int maxLength,
               final @JsonProperty("prefix") String prefix,
               final @JsonProperty("ta") String trustAnchor) {
        this.asn = Asn.parse(asn).asBigInteger().longValue();
        this.maxLength = maxLength;
        this.prefix = prefix;
        this.trustAnchor = TrustAnchor.fromRpkiName(trustAnchor);
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
}
