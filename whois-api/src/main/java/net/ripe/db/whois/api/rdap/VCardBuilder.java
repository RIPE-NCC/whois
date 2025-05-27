package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.vcard.VCard;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardKind;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardName;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardProperty;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardType;
import net.ripe.db.whois.common.domain.CIString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.nCopies;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.ADDRESS;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.EMAIL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.FN;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.GEO;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.KIND;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.ORG;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.TELEPHONE;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.VERSION;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.TEXT;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.URI;

public class VCardBuilder {

    private static final Joiner NEWLINE_JOINER = Joiner.on("\n");
    private final List<VCardProperty> properties = Lists.newArrayList();

    private static final String PARAMETER_KEY = "type";
    private static final Map ABUSE_MAP = ImmutableMap.of(PARAMETER_KEY,"abuse");
    private static final Map PHONE_MAP = ImmutableMap.of(PARAMETER_KEY, "voice");
    private static final Map EMAIL_MAP = ImmutableMap.of(PARAMETER_KEY,"email");
    private static final Map FAX_MAP = ImmutableMap.of(PARAMETER_KEY, "fax");
    private static final Map EMPTY_MAP = ImmutableMap.of();

    public VCardBuilder addAdr(final Set<CIString> addresses) {
        final String label = "label";
        if (!addresses.isEmpty()) {
            properties.add(new VCardProperty(ADDRESS, ImmutableMap.of(label,NEWLINE_JOINER.join(addresses)), TEXT, nCopies(7, ""))); //VCard format 7 empty elements for text
        }
        return this;
    }

    public void addEmail(final Set<CIString> emails) {
        emails.forEach( email -> addProperty(EMAIL, EMAIL_MAP, TEXT, email));
    }

    public VCardBuilder addAbuseMailBox(final CIString abuseMail) {
        if(abuseMail != null) {
            addProperty(EMAIL, ABUSE_MAP, TEXT, abuseMail);
        }
        return this;
    }

    public VCardBuilder addFn(final CIString value) {
        addProperty(FN, EMPTY_MAP, TEXT, value);
        return this;
    }

    public VCardBuilder addGeo(final Set<CIString> geolocs) {
        geolocs.forEach( geo -> addProperty(GEO, EMPTY_MAP, URI, geo));
        return this;
    }

    public VCardBuilder addKind(final VCardKind kind) {
        addProperty(KIND, EMPTY_MAP, TEXT, kind.getValue());
        return this;
    }

    public VCardBuilder addTel(final Set<CIString> phones) {
        phones.forEach( phone -> addProperty(TELEPHONE, PHONE_MAP, getTelType(phone), phone));
        return this;
    }

    public VCardBuilder addFax(final Set<CIString> faxes) {
        faxes.forEach( fax -> addProperty(TELEPHONE, FAX_MAP, getTelType(fax), fax));
        return this;
    }

    public VCardBuilder addVersion() {
        addProperty(VERSION, EMPTY_MAP, TEXT, "4.0");
        return this;
    }

    public VCardBuilder addOrg(final Set<CIString> values) {
        values.forEach( org-> addProperty(ORG, EMPTY_MAP, TEXT, org));
        return this;
    }

    private void addProperty(VCardName name, final Map parameters, final VCardType type, final String value) {
        properties.add(new VCardProperty(name, parameters, type, value));
    }

    private void addProperty(VCardName name, final Map parameters, final VCardType type, final CIString value) {
        addProperty(name, parameters, type, value.toString());
    }

    public VCard build() {
        return new VCard(properties);
    }

    private VCardType getTelType(CIString value) {
        return value.startsWith("tel:") ? URI : TEXT;
    }
}