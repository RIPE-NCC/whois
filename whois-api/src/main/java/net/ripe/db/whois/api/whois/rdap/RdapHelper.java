package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.ObjectFactory;
import net.ripe.db.whois.api.whois.rdap.domain.Person;

import java.util.List;


public class RdapHelper {
    public static List<String> RDAPCONFORMANCE = Lists.newArrayList("rdap_level_0");
    public static ObjectFactory rdapObjectFactory = new ObjectFactory();

    public static Domain createDomain() {
        Domain domain = rdapObjectFactory.createDomain();
        domain.getRdapConformance().addAll(RDAPCONFORMANCE);
        return domain;
    }

    public static Person createPerson() {
        Person person = rdapObjectFactory.createPerson();
        person.getRdapConformance().addAll(RDAPCONFORMANCE);
        return person;
    }

}
