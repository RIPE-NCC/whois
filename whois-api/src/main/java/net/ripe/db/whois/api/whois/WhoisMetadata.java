package net.ripe.db.whois.api.whois;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeTemplate;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.source.SourceContext;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
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

@ExternallyManagedLifecycle
@Component
@Path("/metadata")
public class WhoisMetadata {

    // we cannot get this from metadata, as we have two separate JVMs running for each source
    private final List<Source> SOURCES = ImmutableList.of(
            new Source("ripe").setName("RIPE"),
            new Source("test").setName("TEST"));

    private final List<GrsSource> GRSSOURCES;

    private final Map<String, Template> ATTRIBUTE_TEMPLATES;

    @Autowired
    public WhoisMetadata(SourceContext sourceContext) {

        GRSSOURCES = Lists.newArrayList();
        for (CIString source: sourceContext.getGrsSourceNames()) {
            final String id = source.toLowerCase();
            GRSSOURCES.add(new GrsSource(id).setGrsId(id).setName(id.toUpperCase()));
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
                        .setKey(attributeTemplate.getKeys()));

            }

            Template template = new Template()
                    .setSource(ripeSource)
                    .setType(objectType.getName())
                    .setAttributes(templateAttributes);

            ATTRIBUTE_TEMPLATES.put(objectType.getName(), template);
        }
    }

    /**
     * <p>List available sources</p>
     *
     * <div>Example:</div>
     * <pre>
     *    http://apps.db.ripe.net/whois-beta/metadata/sources.json
     * </pre>
     *
     * <div>Example response in XML:</div>
     * <pre>
     *     &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     *     &lt;whois-resources xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink"
     *                      service="getSupportedDataSources"
     *                      xsi:noNamespaceSchemaLocation="http://apps.db.ripe.net/whois-beta/xsd/whois-resources.xsd"&gt;
     *         &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/metadata/sources"/&gt;
     *         &lt;sources&gt;
     *             &lt;source name="RIPE" id="ripe"/&gt;
     *             &lt;source name="TEST" id="test"/&gt;
     *         &lt;/sources&gt;
     *         &lt;grs-sources&gt;
     *             &lt;source name="TEST-GRS" id="test-grs" grs-id="test-grs"/&gt;
     *         &lt;/grs-sources&gt;
     *     &lt;/whois-resources&gt;
     * </pre>
     *
     * <div>Example response in JSON:</div>
     * <pre>
     *  {
     *    "whois-resources" : {
     *      "service" : "getSupportedDataSources",
     *      "link" : {
     *        "xlink:type" : "locator",
     *        "xlink:href" : "http://apps.db.ripe.net/whois-beta/metadata/sources"
     *      },
     *      "sources" : {
     *        "source" : [ {
     *          "name" : "RIPE",
     *          "id" : "ripe"
     *        }, {
     *          "name" : "TEST",
     *          "id" : "test"
     *        } ]
     *      },
     *      "grs-sources" : {
     *        "source" : [ {
     *          "name" : "TEST-GRS",
     *          "id" : "test-grs",
     *          "grs-id" : "test-grs"
     *        } ]
     *      }
     *    }
     *  }
     * </pre>
     *
     * @return Returns all available sources.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/sources")
    public Response sources() {
        WhoisResources result = new WhoisResources()
            .setService("getSupportedDataSources")
            .setLink(new Link("locator", "http://apps.db.ripe.net/whois-beta/metadata/sources"))
            .setSources(SOURCES)
            .setGrsSources(GRSSOURCES);
        return Response.ok(result).build();
    }

    /**
     * <p>The RPSL template for given object type.</p>
     *
     * <div>Example querying for the template of PERSON:</div>
     * <pre>
     * http://apps.db.ripe.net/whois-beta/metadata/templates/person.xml
     * </pre>
     *
     * <div>Example response in XML:</div>
     * <pre>
     *  &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;
     *  &lt;template-resources xmlns:xlink="http://www.w3.org/1999/xlink" service="getObjectTemplate"&gt;
     *      &lt;link xlink:type="locator" xlink:href="http://apps.db.ripe.net/whois-beta/metadata/templates/person"/&gt;
     *      &lt;templates&gt;
     *          &lt;template type="person"&gt;
     *              &lt;source id="ripe"/&gt;
     *              &lt;attributes&gt;
     *                  &lt;attribute name="person" requirement="MANDATORY" cardinality="SINGLE" keys="LOOKUP_KEY"/&gt;
     *                  &lt;attribute name="address" requirement="MANDATORY" cardinality="MULTIPLE" keys=""/&gt;
     *                  &lt;attribute name="phone" requirement="MANDATORY" cardinality="MULTIPLE" keys=""/&gt;
     *                  &lt;attribute name="fax-no" requirement="OPTIONAL" cardinality="MULTIPLE" keys=""/&gt;
     *                  &lt;attribute name="e-mail" requirement="OPTIONAL" cardinality="MULTIPLE" keys="LOOKUP_KEY"/&gt;
     *                  &lt;attribute name="org" requirement="OPTIONAL" cardinality="MULTIPLE" keys="INVERSE_KEY"/&gt;
     *                  &lt;attribute name="nic-hdl" requirement="MANDATORY" cardinality="SINGLE" keys="PRIMARY_KEY LOOKUP_KEY"/&gt;
     *                  &lt;attribute name="remarks" requirement="OPTIONAL" cardinality="MULTIPLE" keys=""/&gt;
     *                  &lt;attribute name="notify" requirement="OPTIONAL" cardinality="MULTIPLE" keys="INVERSE_KEY"/&gt;
     *                  &lt;attribute name="abuse-mailbox" requirement="OPTIONAL" cardinality="MULTIPLE" keys="INVERSE_KEY"/&gt;
     *                  &lt;attribute name="mnt-by" requirement="MANDATORY" cardinality="MULTIPLE" keys="INVERSE_KEY"/&gt;
     *                  &lt;attribute name="changed" requirement="MANDATORY" cardinality="MULTIPLE" keys=""/&gt;
     *                  &lt;attribute name="source" requirement="MANDATORY" cardinality="SINGLE" keys=""/&gt;
     *              &lt;/attributes&gt;
     *          &lt;/template&gt;
     *      &lt;/templates&gt;
     *  &lt;/template-resources&gt;
     * </pre>
     *
     * <div>Example response in JSON:</div>
     * <pre>
     *  {
     *    "template-resources" : {
     *      "service" : "getObjectTemplate",
     *      "link" : {
     *        "xlink:type" : "locator",
     *        "xlink:href" : "http://apps.db.ripe.net/whois-beta/metadata/templates/person"
     *      },
     *      "templates" : {
     *        "template" : [ {
     *          "type" : "person",
     *          "source" : {
     *            "id" : "ripe"
     *          },
     *          "attributes" : {
     *            "attribute" : [ {
     *              "name" : "person",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "SINGLE",
     *              "keys" : [ "LOOKUP_KEY" ]
     *            }, {
     *              "name" : "address",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ ]
     *            }, {
     *              "name" : "phone",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ ]
     *            }, {
     *              "name" : "fax-no",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ ]
     *            }, {
     *              "name" : "e-mail",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ "LOOKUP_KEY" ]
     *            }, {
     *              "name" : "org",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ "INVERSE_KEY" ]
     *            }, {
     *              "name" : "nic-hdl",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "SINGLE",
     *              "keys" : [ "PRIMARY_KEY", "LOOKUP_KEY" ]
     *            }, {
     *              "name" : "remarks",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ ]
     *            }, {
     *              "name" : "notify",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ "INVERSE_KEY" ]
     *            }, {
     *              "name" : "abuse-mailbox",
     *              "requirement" : "OPTIONAL",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ "INVERSE_KEY" ]
     *            }, {
     *              "name" : "mnt-by",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ "INVERSE_KEY" ]
     *            }, {
     *              "name" : "changed",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "MULTIPLE",
     *              "keys" : [ ]
     *            }, {
     *              "name" : "source",
     *              "requirement" : "MANDATORY",
     *              "cardinality" : "SINGLE",
     *              "keys" : [ ]
     *            } ]
     *          }
     *        } ]
     *      }
     *    }
     *  }
     * </pre>
     *
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
                .setService("getObjectTemplate")
                .setLink(new Link("locator", "http://apps.db.ripe.net/whois-beta/metadata/templates/"+objectType))
                .setTemplates(Collections.singletonList(template));

        return Response.ok(result).build();
    }
}
