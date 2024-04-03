package net.ripe.db.whois.common.rpki;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Roas {

    private List<Roa> roas;

    public List<Roa> getRoas() {
        return roas;
    }

    public void setRoas(List<Roa> roas) {
        this.roas = roas;
    }
}
