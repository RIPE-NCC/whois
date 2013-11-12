package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "locations",
    "languages"
})
@XmlRootElement(name = "geolocation-attributes")
public class GeolocationAttributes {
    @XmlElement(name = "location")
    private List<Location> locations;
    @XmlElement(name = "language")
    private List<Language> languages;

    public GeolocationAttributes(final Location location, final List<Language> languages) {
        this.locations = Lists.newArrayList(location);
        this.languages = languages;
    }

    public GeolocationAttributes() {
        // required no-arg constructor
    }
}
