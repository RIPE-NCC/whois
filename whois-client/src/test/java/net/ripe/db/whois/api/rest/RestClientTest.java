package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import java.util.Iterator;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {

    private static final RpslObject MNTNER_OBJECT = RpslObject.parse("mntner: OWNER-MNT\nsource: RIPE");

    private Client clientMock;
    private WhoisResources whoisResourcesMock;
    private AbuseResources abuseResourcesMock;
    private AbuseContact abuseContactMock;
    private RestClient subject;
    private String url;

    @Before
    public void setup() {
        clientMock = mock(Client.class);

        whoisResourcesMock = mock(WhoisResources.class);
        WhoisObject whoisObject = mock(WhoisObject.class);
        when(whoisObject.getAttributes()).thenReturn(Lists.newArrayList(new Attribute("mntner", "OWNER-MNT"), new Attribute("source", "RIPE")));
        when(whoisResourcesMock.getWhoisObjects()).thenReturn(Lists.newArrayList(whoisObject));

        abuseContactMock = mock(AbuseContact.class);
        when(abuseContactMock.getEmail()).thenReturn("user@host.org");
        abuseResourcesMock = mock(AbuseResources.class);
        when(abuseResourcesMock.getAbuseContact()).thenReturn(abuseContactMock);

        when(clientMock.target(any(String.class))).thenAnswer(new Answer<WebTarget>() {
            @Override
            public WebTarget answer(InvocationOnMock invocation) throws Throwable {
                url = (String) invocation.getArguments()[0];
                final WebTarget webTarget = mock(WebTarget.class);
                final Builder builder = mock(Builder.class);
                when(builder.post(any(Entity.class), any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return ((Entity) invocation.getArguments()[0]).getEntity();
                    }
                });
                when(builder.get(any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        final Class type = (Class) invocation.getArguments()[0];
                        if (type.equals(WhoisResources.class)) {
                            return whoisResourcesMock;
                        }
                        if (type.equals(AbuseResources.class)) {
                            return abuseResourcesMock;
                        }
                        return null;
                    }
                });
                when(builder.put(any(Entity.class), any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return ((Entity) invocation.getArguments()[0]).getEntity();
                    }
                });
                when(webTarget.request()).thenReturn(builder);
                return webTarget;
            }
        });

        subject = new RestClient();
        subject.setRestApiUrl("http://localhost");
        subject.setSource("RIPE");
        subject.setClient(clientMock);
    }

    @Test
    public void create() {
        final RpslObject result = subject.create(MNTNER_OBJECT, "password1");

        assertThat(url, is("http://localhost/RIPE/mntner?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_with_multiple_passwords() {
        final RpslObject result = subject.create(MNTNER_OBJECT, "password1", "password2", "password3");

        assertThat(url, is("http://localhost/RIPE/mntner?password=password1&password=password2&password=password3"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    // TODO: create w/ error response

    @Test
    public void create_override() {
        final RpslObject result = subject.createOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner?override=override1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void delete() {
        subject.delete(MNTNER_OBJECT, "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1"));
    }

    @Test
    public void delete_override() {
        subject.deleteOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?override=override1"));
    }

    @Test
    public void lookup() {
        final RpslObject result = subject.lookup(ObjectType.MNTNER, "OWNER-MNT");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?unfiltered"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookup_with_password() {
        final RpslObject result = subject.lookup(ObjectType.MNTNER, "OWNER-MNT", "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1&unfiltered"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookup_abuse_contact() {
        final AbuseContact result = subject.lookupAbuseContact("10.0.0.1");

        assertThat(url, is("http://localhost/abuse-contact/10.0.0.1"));
        assertThat(result.getEmail(), is("user@host.org"));
    }

    @Test
    public void search() {
        final Iterator<RpslObject> results = subject.search("OWNER-MNT",
                Sets.newHashSet("RIPE-GRS", "ARIN-GRS"),
                Sets.newHashSet(AttributeType.ADMIN_C, AttributeType.TECH_C),
                Sets.newHashSet("include-tag1", "include-tag2"),
                Sets.newHashSet("exclude-tag1", "exclude-tag2"),
                Sets.newHashSet(ObjectType.MNTNER),
                Sets.newHashSet(QueryFlag.ALL_SOURCES, QueryFlag.BRIEF, QueryFlag.INVERSE)).iterator();

        assertThat(results.next(), is(MNTNER_OBJECT));
        assertThat(results.hasNext(), is(false));
        assertThat(url, containsString("http://localhost/search?"));
        assertThat(url, containsString("query-string=OWNER-MNT"));
        assertThat(url, containsString("source=ARIN-GRS"));
        assertThat(url, containsString("source=RIPE-GRS"));
        assertThat(url, containsString("inverse-attribute=tech-c"));
        assertThat(url, containsString("inverse-attribute=admin-c"));
        assertThat(url, containsString("include-tag=include-tag1"));
        assertThat(url, containsString("include-tag=include-tag2"));
        assertThat(url, containsString("exclude-tag=exclude-tag1"));
        assertThat(url, containsString("exclude-tag=exclude-tag2"));
        assertThat(url, containsString("flags=all-sources"));
        assertThat(url, containsString("flags=inverse"));
        assertThat(url, containsString("flags=brief"));
        assertThat(url, containsString("type-filter=mntner"));
    }

    @Test
    public void update() {
        final RpslObject result = subject.update(MNTNER_OBJECT, "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void update_override() {
       final RpslObject result = subject.updateOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?override=override1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }
}
