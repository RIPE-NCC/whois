package net.ripe.db.abusec;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import net.ripe.db.whois.api.rest.client.RestClient;
import net.ripe.db.whois.api.rest.mapper.AttributeMapper;
import net.ripe.db.whois.api.rest.mapper.DirtyClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.FormattedClientAttributeMapper;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.io.RpslObjectFileReader;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import java.util.Map;
import java.util.Set;

public class MultipleAbuseMailboxAttributes {

    private static final String REST_BASE_URL = "https://rest.db.ripe.net";
    private static final String SOURCE = "RIPE";

    private static final String CHANGED_BY = "ripe-dbm@ripe.net";

    public static void main(String[] args) throws Exception {

        final Set<String> rolesWithMultipleAbuseMailboxes = Sets.newHashSet();

        // find roles with multiple abuse-mailbox attributes (which were allowed before the current abuse-c implementation).

        for (String nextRole : new RpslObjectFileReader(Resources.getResource("ripe.db.role.gz").getFile())) {
            final RpslObject role = RpslObject.parse(nextRole);
            if (role.findAttributes(AttributeType.ABUSE_MAILBOX).size() > 1) {
                final String key = role.getKey().toString().toUpperCase();
                rolesWithMultipleAbuseMailboxes.add(key);
            }
        }

        // ignore roles which are already referenced by an organisation

        for (String nextOrg : new RpslObjectFileReader(Resources.getResource("ripe.db.organisation.gz").getFile())) {
            final RpslObject orgObject = RpslObject.parse(nextOrg);
            if (orgObject.containsAttribute(AttributeType.ABUSE_C)) {
                final String key = orgObject.getValueForAttribute(AttributeType.ABUSE_C).toString().toUpperCase();
                if (rolesWithMultipleAbuseMailboxes.contains(key)) {
                    System.out.println("organisation: " + orgObject.getKey() + " already references syntactically incorrect role: " + key + " - this role must be updated manually!");
                    rolesWithMultipleAbuseMailboxes.remove(key);
                }
            }
        }

        final RestClient restClient = createRestClient(REST_BASE_URL, SOURCE);

        // update roles - convert ALL abuse-mailbox attributes into remarks

        for (String nextRole : rolesWithMultipleAbuseMailboxes) {
            final RpslObject roleObject = restClient.request().lookup(ObjectType.ROLE, nextRole);

            Map<RpslAttribute, RpslAttribute> replacements = Maps.newHashMap();

            for (RpslAttribute nextAttribute : roleObject.findAttributes(AttributeType.ABUSE_MAILBOX)) {
                replacements.put(nextAttribute, new RpslAttribute(AttributeType.REMARKS, String.format("%s: %s", nextAttribute.getKey(), nextAttribute.getValue())));
            }

            final RpslObject updatedRoleObject = new RpslObjectBuilder(roleObject)
                    .replaceAttributes(replacements)
                    .addAttributeSorted(new RpslAttribute(AttributeType.CHANGED, CHANGED_BY))
                    .get();

            System.out.println(updatedRoleObject.toString() + "\n\n");
        }
    }

    private static RestClient createRestClient(final String baseUrl, final String source) {
        final RestClient restClient = new RestClient(baseUrl, source);
        restClient.setWhoisObjectMapper(
                new WhoisObjectMapper(
                        baseUrl,
                        new AttributeMapper[]{
                                new FormattedClientAttributeMapper(),
                                new DirtyClientAttributeMapper()
                        }));
        return restClient;
    };
}
