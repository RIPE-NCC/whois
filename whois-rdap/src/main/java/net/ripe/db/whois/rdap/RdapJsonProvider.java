package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.cfg.Annotations;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

@Provider
@Consumes({MediaType.APPLICATION_JSON, "application/rdap+json"})
@Produces({MediaType.APPLICATION_JSON, "application/rdap+json"})
public class RdapJsonProvider extends JacksonJsonProvider {

    private static final Annotations[] DEFAULT_ANNOTATIONS = new Annotations[] {Annotations.JACKSON, Annotations.JAKARTA_XML_BIND};

    public RdapJsonProvider() {
        super(null, DEFAULT_ANNOTATIONS);
    }

    public RdapJsonProvider(Annotations... annotations) {
        this(null, annotations);
    }

    public RdapJsonProvider(ObjectMapper objectMapper, Annotations[] annotations) {
        super(objectMapper, annotations);
    }
}
