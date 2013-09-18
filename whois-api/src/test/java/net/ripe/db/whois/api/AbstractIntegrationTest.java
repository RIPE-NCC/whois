package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.JettyConfig;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.dao.jdbc.AbstractDatabaseHelperTest;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public abstract class AbstractIntegrationTest extends AbstractDatabaseHelperTest {
    @Autowired protected JettyConfig jettyConfig;
    @Autowired protected List<ApplicationService> applicationServices;

    @Before
    public void startServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.start();
        }
    }

    @After
    public void stopServer() throws Exception {
        for (final ApplicationService applicationService : applicationServices) {
            applicationService.stop(true);
        }
    }

    public int getPort(final Audience audience) {
        return jettyConfig.getPort(audience);
    }

    public String doGetRequest(final String url, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();

        assertThat(connection.getResponseCode(), is(responseCode));

        return readResponse(connection);
    }

    public String doPostRequest(final String url, final String content, final MediaType contentType, final int responseCode) throws IOException {
        return doPostOrPutRequest(url, "POST", content, contentType, null, responseCode);
    }

    public String doPutRequest(final String url, final String content, final MediaType contentType, final int responseCode) throws IOException {
        return doPostOrPutRequest(url, "PUT", content, contentType, null, responseCode);
    }

    private String doPostOrPutRequest(final String url, final String method, final String data, final MediaType contentType, final MediaType accepts, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty(HttpHeaders.CONTENT_LENGTH, Integer.toString(data.length()));
        connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, contentType.toString());
        if (accepts != null) {
            connection.setRequestProperty(HttpHeaders.ACCEPT, accepts.toString());
        }

        connection.setDoInput(true);
        connection.setDoOutput(true);

        Writer writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(data);
        writer.close();

        assertThat(connection.getResponseCode(), is(responseCode));

        return readResponse(connection);
    }

    public void doDeleteRequest(final String url, final int responseCode) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setDoOutput(true);

        assertThat(connection.getResponseCode(), is(responseCode));
    }

    public String readResponse(final HttpURLConnection connection) throws IOException {
        InputStream inputStream = null;

        try {
            StringBuilder builder = new StringBuilder();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            BufferedReader responseReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = responseReader.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            return builder.toString();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
