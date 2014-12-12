package net.ripe.db.whois.api.rest.domain;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "type-filters")
@XmlAccessorType(XmlAccessType.FIELD)
public class TypeFilters {

    @XmlElement(name = "type-filter")
    private List<TypeFilter> typeFilters;

    public TypeFilters(final List<TypeFilter> typeFilters) {
        this.typeFilters = typeFilters;
    }

    public TypeFilters(final Collection<String> typeFilters) {
        this.typeFilters = Lists.newArrayList();
        for (String typeFilter : typeFilters) {
            this.typeFilters.add(new TypeFilter(typeFilter));
        }
    }

    public TypeFilters() {
        this.typeFilters = Lists.newArrayList();
    }

    public List<TypeFilter> getTypeFilters() {
        return typeFilters;
    }
}
