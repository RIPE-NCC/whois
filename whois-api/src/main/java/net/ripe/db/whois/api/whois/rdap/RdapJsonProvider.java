package net.ripe.db.whois.api.whois.rdap;

import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

@Provider
@Consumes({MediaType.APPLICATION_JSON, "application/rdap+json"})       // TODO: [ES] support text/json
@Produces({MediaType.APPLICATION_JSON, "application/rdap+json"})
public class RdapJsonProvider extends JacksonJsonProvider {

    private static final Annotations[] DEFAULT_ANNOTATIONS = new Annotations[] {Annotations.JACKSON, Annotations.JAXB};

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
