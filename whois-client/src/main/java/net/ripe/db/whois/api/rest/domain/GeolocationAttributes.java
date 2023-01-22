package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;

@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "locations",
    "languages"
})
@XmlRootElement(name = "geolocation-attributes")
public class GeolocationAttributes {
    @XmlElement(name = "location")
    private List<Location> locations = Lists.newArrayList();
    @XmlElement(name = "language")
    private List<Language> languages = Lists.newArrayList();

    public GeolocationAttributes(@Nullable final Location location, @Nullable final List<Language> languages) {
        this.locations.add(location);
        this.languages.addAll(languages == null ? Collections.EMPTY_LIST : languages);
    }

    public GeolocationAttributes() {
        // required no-arg constructor
    }
}
