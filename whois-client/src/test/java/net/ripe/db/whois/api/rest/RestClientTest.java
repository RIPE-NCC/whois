package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.Arg;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Iterator;

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
    private RestClient subject;
    private String url;

    @Before
    public void setup() {
        clientMock = mock(Client.class);

        whoisResourcesMock = mock(WhoisResources.class);
        WhoisObject whoisObject = mock(WhoisObject.class);
        when(whoisObject.getAttributes()).thenReturn(Lists.newArrayList(new Attribute("mntner", "OWNER-MNT"), new Attribute("source", "RIPE")));
        when(whoisResourcesMock.getWhoisObjects()).thenReturn(Lists.newArrayList(whoisObject));

        final AbuseContact abuseContactMock = mock(AbuseContact.class);
        when(abuseContactMock.getEmail()).thenReturn("user@host.org");
        abuseResourcesMock = mock(AbuseResources.class);
        when(abuseResourcesMock.getAbuseContact()).thenReturn(abuseContactMock);

        subject = new RestClient();
        subject.setRestApiUrl("http://localhost");
        subject.setSource("RIPE");
        subject.setClient(clientMock);
    }

    @Test
    public void create() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.create(MNTNER_OBJECT, "password1");

        assertThat(url, is("http://localhost/RIPE/mntner?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_with_multiple_passwords() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.create(MNTNER_OBJECT, "password1", "password2", "password3");

        assertThat(url, is("http://localhost/RIPE/mntner?password=password1&password=password2&password=password3"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_with_error() {
        final BadRequestException exceptionMock = mock(BadRequestException.class);
        final WhoisResources whoisResources = new WhoisResources();
        final ErrorMessage errorMessage = new ErrorMessage("Error", null, "Invalid argument %s", Lists.newArrayList(new Arg("flag")));
        whoisResources.setErrorMessages(Lists.newArrayList(errorMessage));
        final Response responseMock = mock(Response.class);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.readEntity(WhoisResources.class)).thenReturn(whoisResources);
        mockWithException(exceptionMock);

        try {
            subject.create(MNTNER_OBJECT);
            fail();
        } catch (RestClientException e) {
            assertThat(url, is("http://localhost/RIPE/mntner"));
            assertThat(e.getErrorMessages(), hasSize(1));
            assertThat(e.getErrorMessages().get(0), is(errorMessage));
        }
    }

    @Test
    public void create_override() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.createOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner?override=override1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void delete() {
        mockWithResponse(null);

        subject.delete(MNTNER_OBJECT, "reason1", "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1&reason=reason1"));
    }

    @Test
    public void delete_override() {
        mockWithResponse(null);

        subject.deleteOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?override=override1"));
    }

    @Test
    public void lookup() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.lookup(ObjectType.MNTNER, "OWNER-MNT");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookup_with_password() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.lookup(ObjectType.MNTNER, "OWNER-MNT", "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1&unfiltered"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookupWhoisObject() {
        mockWithResponse(whoisResourcesMock);

        final WhoisObject result = subject.lookupWhoisObject(ObjectType.MNTNER, "OWNER-MNT");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT"));
        assertThat(result.getAttributes().iterator().next().getValue(), is("OWNER-MNT"));
    }

    @Test
    public void lookupWhoisObjectWithPassword() {
        mockWithResponse(whoisResourcesMock);

        final WhoisObject result = subject.lookupWhoisObject(ObjectType.MNTNER, "OWNER-MNT", "mylittlepony");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=mylittlepony&unfiltered"));
        assertThat(result.getAttributes().iterator().next().getValue(), is("OWNER-MNT"));
    }

    @Test
    public void lookup_abuse_contact() {
        mockWithResponse(abuseResourcesMock);

        final AbuseContact result = subject.lookupAbuseContact("10.0.0.1");

        assertThat(url, is("http://localhost/abuse-contact/10.0.0.1"));
        assertThat(result.getEmail(), is("user@host.org"));
    }

    @Test
    public void lookup_abuse_contact_error() {
        final BadRequestException exceptionMock = mock(BadRequestException.class);
        final Response responseMock = mock(Response.class);
        when(exceptionMock.getResponse()).thenReturn(responseMock);
        when(responseMock.readEntity(String.class)).thenReturn("bad request");
        mockWithException(exceptionMock);

        try {
            subject.lookupAbuseContact("10.0.0.1");
            fail();
        } catch (RestClientException e) {
            assertThat(url, is("http://localhost/abuse-contact/10.0.0.1"));
            assertThat(e.getErrorMessages(), hasSize(1));
            assertThat(e.getErrorMessages().get(0).getText(), is("bad request"));
        }
    }

    @Test
    public void search() {
        mockWithResponse(whoisResourcesMock);

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
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.update(MNTNER_OBJECT, "password1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void update_override() {
        mockWithResponse(whoisResourcesMock);

       final RpslObject result = subject.updateOverride(MNTNER_OBJECT, "override1");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?override=override1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    // helper methods

    private void mockWithException(final ClientErrorException exceptionMock) {
        Mockito.reset(clientMock);
        when(clientMock.target(any(String.class))).thenAnswer(new Answer<WebTarget>() {
            @Override
            public WebTarget answer(InvocationOnMock invocation) throws Throwable {
                url = (String) invocation.getArguments()[0];
                final WebTarget webTarget = mock(WebTarget.class);
                final Builder builder = mock(Builder.class);
                when(builder.get(any(Class.class))).thenThrow(exceptionMock);
                when(builder.post(any(Entity.class), any(Class.class))).thenThrow(exceptionMock);
                when(builder.put(any(Entity.class))).thenThrow(exceptionMock);
                when(webTarget.request()).thenReturn(builder);
                return webTarget;
            }
        });
    }

    private void mockWithResponse(final Object objectMock) {
        Mockito.reset(clientMock);
        when(clientMock.target(any(String.class))).thenAnswer(new Answer<WebTarget>() {
            @Override
            public WebTarget answer(InvocationOnMock invocation) throws Throwable {
                url = (String) invocation.getArguments()[0];
                final WebTarget webTarget = mock(WebTarget.class);
                final Builder builder = mock(Builder.class);
                when(builder.post(any(Entity.class), any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        //return ((Entity) invocation.getArguments()[0]).getEntity();
                        return objectMock;
                    }
                });
                when(builder.get(any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return objectMock;
                    }
                });
                when(builder.put(any(Entity.class), any(Class.class))).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        return objectMock;
                    }
                });
                when(builder.delete(any(Class.class))).thenReturn(objectMock);
                when(webTarget.request()).thenReturn(builder);
                return webTarget;
            }
        });
    }
}
