package net.ripe.db.whois.api.rest;

import com.google.common.collect.Lists;
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
import org.apache.commons.lang.StringUtils;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        final RpslObject result = subject.request()
                .addParam("password", "password1")
                .create(MNTNER_OBJECT);

        assertThat(url, is("http://localhost/RIPE/mntner?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_with_multiple_passwords() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.request()
                .addParams("password", "password1", "password2", "password3")
                .create(MNTNER_OBJECT);

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
            subject.request().create(MNTNER_OBJECT);
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

        final RpslObject result = subject.request()
                .addParam("override", "override1")
                .create(MNTNER_OBJECT);

        assertThat(url, is("http://localhost/RIPE/mntner?override=override1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void create_with_notifier() {
        final NotifierCallback notifier = mock(NotifierCallback.class);
        mockWithResponse(whoisResourcesMock);
        final List<ErrorMessage> messages = Collections.singletonList(new ErrorMessage("Info", null, "test message", Collections.<Arg>emptyList()));
        when(whoisResourcesMock.getErrorMessages()).thenReturn(messages);

        subject.request()
                .setNotifier(notifier)
                .addParam("password", "password1")
                .create(MNTNER_OBJECT);

        verify(notifier).notify(messages);
    }

    @Test
    public void delete() {
        mockWithResponse(whoisResourcesMock);

        subject.request()
                .addParam("reason", "reason1")
                .addParam("password", "password1")
                .delete(MNTNER_OBJECT);

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?reason=reason1&password=password1"));
    }

    @Test
    public void delete_override() {
        mockWithResponse(whoisResourcesMock);

        subject.request()
                .addParam("override", "override1")
                .delete(MNTNER_OBJECT);

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?override=override1"));
    }

    @Test
    public void lookup() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.request()
                .lookup(ObjectType.MNTNER, "OWNER-MNT");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?unfiltered"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookup_with_password() {
        mockWithResponse(whoisResourcesMock);

        final RpslObject result = subject.request()
                .addParam("password", "password1")
                .lookup(ObjectType.MNTNER, "OWNER-MNT");

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?unfiltered&password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void lookup_abuse_contact() {
        mockWithResponse(abuseResourcesMock);

        final AbuseContact result = subject.request().lookupAbuseContact("10.0.0.1");

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
            subject.request().lookupAbuseContact("10.0.0.1");
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

        final Collection<RpslObject> results = subject.request()
                .addParam("query-string", "OWNER-MNT")
                .addParams("source", "RIPE-GRS", "ARIN-GRS")
                .addParams("inverse-attribute", AttributeType.ADMIN_C.getName(), AttributeType.TECH_C.getName())
                .addParams("include-tag", "include-tag1", "include-tag2")
                .addParams("exclude-tag", "exclude-tag1", "exclude-tag2")
                .addParams("type-filter", ObjectType.MNTNER.getName())
                .addParams("flags", QueryFlag.ALL_SOURCES.getName(), QueryFlag.BRIEF.getName(), QueryFlag.INVERSE.getName())
                .search();

        assertThat(results, contains(MNTNER_OBJECT));
        assertThat(results, hasSize(1));
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

        final RpslObject result = subject.request()
                .addParam("password", "password1")
                .update(MNTNER_OBJECT);

        assertThat(url, is("http://localhost/RIPE/mntner/OWNER-MNT?password=password1"));
        assertThat(result.getKey(), is(CIString.ciString("OWNER-MNT")));
        assertThat(result.getType(), is(ObjectType.MNTNER));
    }

    @Test
    public void update_override() {
        mockWithResponse(whoisResourcesMock);

       final RpslObject result = subject.request()
               .addParam("override", "override1")
               .update(MNTNER_OBJECT);

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

                when(webTarget.queryParam(any(String.class), anyVararg())).thenAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        final Iterator iterator = Arrays.asList(invocation.getArguments()).iterator();
                        final String name = (String)iterator.next();
                        url = url + (url.contains("?") ? "&" : "?");
                        while (iterator.hasNext()) {
                            url += name;
                            if (iterator.hasNext()) {
                                String value = iterator.next().toString();
                                url += StringUtils.isNotBlank(value) ? "="+ value : "";
                            }
                            url += iterator.hasNext() ? "&" : "";
                        }
                        return invocation.getMock();
                    }
                });

                when(webTarget.request()).thenReturn(builder);
                return webTarget;
            }
        });
    }
}
