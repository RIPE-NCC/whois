package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.vcard.VCard;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardKind;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardName;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardProperty;
import net.ripe.db.whois.api.rdap.domain.vcard.VCardType;
import net.ripe.db.whois.common.domain.CIString;

import java.util.HashMap;
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
import static net.ripe.db.whois.api.rdap.domain.vcard.VCardType.WORK;

public class VCardBuilder {

    private static final Joiner NEWLINE_JOINER = Joiner.on("\n");
    private final List<VCardProperty> properties = Lists.newArrayList();

    private static final String PARAMETER_KEY = "type";

    public VCardBuilder addAdr(final Set<CIString> addresses) {
        final String label = "label";
        final String labelType = "type";
        if (!addresses.isEmpty()) {
            final Map<String, String> addressMap = new HashMap<>();
            addressMap.put(label,NEWLINE_JOINER.join(addresses));
            addressMap.put(labelType, WORK.getValue());

            properties.add(new VCardProperty(ADDRESS, addCommonProperties(addressMap), TEXT, nCopies(7, ""))); //VCard format 7 empty elements for text
        }
        return this;
    }

    public VCardBuilder addEmail(final Set<CIString> emails) {
        final Map<String, String> emailMap = new HashMap<>();
        emailMap.put(PARAMETER_KEY,"email, work");

        emails.forEach( email -> addProperty(EMAIL, addCommonProperties(emailMap), TEXT, email));
        return this;
    }

    public VCardBuilder addAbuseMailBox(final CIString abuseMail) {
        if(abuseMail != null) {
            final Map<String, String> abuseMap = new HashMap<>();
            abuseMap.put(PARAMETER_KEY,"abuse, work");

            addProperty(EMAIL, addCommonProperties(abuseMap), TEXT, abuseMail);
        }
        return this;
    }

    public VCardBuilder addFn(final CIString value) {
        addProperty(FN, addCommonProperties(new HashMap<>()), TEXT, value);
        return this;
    }

    public void addGeo(final Set<CIString> geolocs) {
        geolocs.forEach( geo -> addProperty(GEO, addCommonProperties(new HashMap<>()), URI, geo));
    }

    public VCardBuilder addKind(final VCardKind kind) {
        addProperty(KIND, addCommonProperties(new HashMap<>()), TEXT, kind.getValue());
        return this;
    }

    public VCardBuilder addTel(final Set<CIString> phones) {
        final Map<String, String> telMap = new HashMap<>();
        telMap.put(PARAMETER_KEY,"voice");

        phones.forEach( phone -> addProperty(TELEPHONE, addCommonProperties(telMap), getTelType(phone), phone));
        return this;
    }

    public VCardBuilder addFax(final Set<CIString> faxes) {
        Map<String, String> faxMap = new HashMap<>();
        faxMap.put(PARAMETER_KEY, "fax");
        faxes.forEach( fax -> addProperty(TELEPHONE, addCommonProperties(faxMap), getTelType(fax), fax));
        return this;
    }

    public VCardBuilder addVersion() {
        addProperty(VERSION, addCommonProperties(new HashMap<>()), TEXT, "4.0");
        return this;
    }

    public VCardBuilder addOrg(final Set<CIString> values) {
        values.forEach( org-> addProperty(ORG, addCommonProperties(new HashMap<>()), TEXT, org));
        return this;
    }

    private Map<String, String> addCommonProperties(Map<String, String> properties){
        properties.put("language", "");
        properties.put("altid", "");
        properties.put("pref", "");

        return properties;
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