package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public Tel createTel() {
        return new Tel();
    }

    public Categories createCategories() {
        return new Categories();
    }

    public Tz createTz() {
        return new Tz();
    }

    public Lang createLang() {
        return new Lang();
    }

    public Email createEmail() {
        return new Email();
    }

    public Logo createLogo() {
        return new Logo();
    }

    public Birth createBirth() {
        return new Birth();
    }

    public Role createRole() {
        return new Role();
    }

    public Key createKey() {
        return new Key();
    }

    public Fn createFn() {
        return new Fn();
    }

    public N createN() {
        return new N();
    }

    public Note createNote() {
        return new Note();
    }

    public Caluri createCaluri() {
        return new Caluri();
    }

    public Title createTitle() {
        return new Title();
    }

    public AdrEntryValueType createAdrEntryValueType() {
        return new AdrEntryValueType();
    }

    public Gender createGender() {
        return new Gender();
    }

    public Geo createGeo() {
        return new Geo();
    }

    public Vcard createVcard() {
        return new Vcard();
    }

    public Adr createAdr() {
        return new Adr();
    }

    public Anniversary createAnniversary() {
        return new Anniversary();
    }

    public Version createVersion() {
        return new Version();
    }

    public NValueType createNValueType() {
        return new NValueType();
    }

    public Org createOrg() {
        return new Org();
    }

    public Url createUrl() {
        return new Url();
    }

    public Kind createKind() {
        return new Kind();
    }

    public Bday createBday() {
        return new Bday();
    }

    public NValueType.Honorifics createNValueTypeHonorifics() {
        return new NValueType.Honorifics();
    }

    public Caladruri createCaladruri() {
        return new Caladruri();
    }

    public Nickname createNickname() {
        return new Nickname();
    }
}
