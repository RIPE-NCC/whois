package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.rdap.domain.vcard.VCard;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardKind;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.EMAIL;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.ADDRESS;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.KIND;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.FN;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.GEO;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.TELEPHONE;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.VERSION;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardName.ORG;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.TEXT;
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.URI;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardName;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardType;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardProperty;
import net.ripe.db.whois.common.domain.CIString;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class VCardBuilder {

    private static final Joiner NEWLINE_JOINER = Joiner.on("\n");
    private final List<VCardProperty> properties = Lists.newArrayList();

    private static final String PARAMETER_KEY = "type";
    private static final Map abuseMap = ImmutableMap.of(PARAMETER_KEY,"abuse");
    private static final Map emailMap = ImmutableMap.of(PARAMETER_KEY,"email");
    private static final Map phoneMap = ImmutableMap.of(PARAMETER_KEY, "voice");
    private static final Map faxMap = ImmutableMap.of(PARAMETER_KEY, "fax");

    public VCardBuilder addAdr(final Set<CIString> addresses) {
        if (!addresses.isEmpty()) {
            addProperty(ADDRESS, ImmutableMap.of("label",NEWLINE_JOINER.join(addresses)), TEXT, null);
        }
        return this;
    }

    public VCardBuilder addEmail(final Set<CIString> emails) {
        emails.forEach( email -> addProperty(EMAIL, emailMap, TEXT, email.toString()));
        return this;
    }

    public VCardBuilder addAbuseMailBox(final CIString abuseMail) {
        if(abuseMail != null) {
            addProperty(EMAIL, abuseMap, TEXT, abuseMail.toString());
        }
        return this;
    }

    public VCardBuilder addFn(final CIString value) {
        addProperty(FN, Maps.newHashMap(), TEXT, value.toString());
        return this;
    }

    public VCardBuilder addGeo(final Set<CIString> geolocs) {
        geolocs.forEach( geo -> addProperty(GEO, Maps.newHashMap(), URI, geo.toString()));
        return this;
    }

    public VCardBuilder addKind(final VCardKind kind) {
        addProperty(KIND, Maps.newHashMap(), TEXT, kind.getValue());
        return this;
    }

    public VCardBuilder addTel(final Set<CIString> phones) {
        phones.forEach( phone -> addProperty(TELEPHONE, phoneMap, getTelType(phone), phone.toString()));
        return this;
    }

    public VCardBuilder addFax(final Set<CIString> faxes) {
        faxes.forEach( fax -> addProperty(TELEPHONE, faxMap, getTelType(fax), fax.toString()));
        return this;
    }

    public VCardBuilder addVersion() {
        addProperty(VERSION, Maps.newHashMap(), TEXT, "4.0");
        return this;
    }

    public VCardBuilder addOrg(final Set<CIString> values) {
        values.forEach( org-> addProperty(ORG, Maps.newHashMap(), TEXT, org.toString()));
        return this;
    }

    private void addProperty(VCardName name, final Map parameters, final VCardType type, final String value) {
        properties.add(new VCardProperty(name, parameters, type, value));
    }

    public VCard build() {
        return new VCard(properties);
    }

    private VCardType getTelType(CIString value) {
        return value.startsWith("tel:") ? URI : TEXT;
    }
}