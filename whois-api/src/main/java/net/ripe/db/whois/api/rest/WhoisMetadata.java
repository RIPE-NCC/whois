package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Service;
import net.ripe.db.whois.api.rest.domain.Source;
import net.ripe.db.whois.api.rest.domain.Template;
import net.ripe.db.whois.api.rest.domain.TemplateAttribute;
import net.ripe.db.whois.api.rest.domain.TemplateResources;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Path("/metadata")
public class WhoisMetadata {

    private final List<Source> SOURCES = Lists.newArrayList(new Source("ripe"), new Source("test"));

    private final Map<String, Template> ATTRIBUTE_TEMPLATES;

    @Autowired
    public WhoisMetadata(SourceContext sourceContext) {

        for (CIString source: sourceContext.getGrsSourceNames()) {
            final String id = source.toLowerCase();
            SOURCES.add(new Source(id));
        }

        Source ripeSource = new Source("ripe");

        ATTRIBUTE_TEMPLATES = Maps.newHashMap();
        for (ObjectType objectType : ObjectType.values()) {
            ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);
            List<TemplateAttribute> templateAttributes = Lists.newArrayList();

            for (AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {

                templateAttributes.add(new TemplateAttribute()
                        .setName(attributeTemplate.getAttributeType().getName())
                        .setCardinality(attributeTemplate.getCardinality())
                        .setRequirement(attributeTemplate.getRequirement())
                        .setKeys(attributeTemplate.getKeys()));

            }

            Template template = new Template()
                    .setSource(ripeSource)
                    .setType(objectType.getName())
                    .setAttributes(templateAttributes);

            ATTRIBUTE_TEMPLATES.put(objectType.getName(), template);
        }
    }

    /**
     * @return Returns all available sources.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/sources")
    public Response sources() {
        WhoisResources result = new WhoisResources()
            .setService(new Service("getSupportedDataSources"))
            .setLink(new Link("locator", "http://rest.db.ripe.net/metadata/sources"))
            .setSources(SOURCES);
        return Response.ok(result).build();
    }

    /**
     * @param objectType The object type for which the template is requested
     * @return Returns the object template for requested type
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/templates/{objectType}")
    public Response templates(@PathParam("objectType") String objectType) {
        final Template template = ATTRIBUTE_TEMPLATES.get(objectType);
        if (template == null) {
            return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
        }

        TemplateResources result = new TemplateResources()
                .setService(new Service("getObjectTemplate"))
                .setLink(new Link("locator", "http://rest.db.ripe.net/metadata/templates/"+objectType))
                .setTemplates(Collections.singletonList(template));

        return Response.ok(result).build();
    }
}
