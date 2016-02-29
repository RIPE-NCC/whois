package net.ripe.db.whois.api.transfer;


import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.StreamingHelper;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class ResponseHandling {

    static javax.ws.rs.core.Response createResponse(final HttpServletRequest request, final WhoisResources whoisResources, final javax.ws.rs.core.Response.Status status) {
        final javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.status(status);
        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }

    static javax.ws.rs.core.Response createResponse(final HttpServletRequest request, final String errorMessage, final javax.ws.rs.core.Response.Status status) {
        WhoisResources whoisResources = new WhoisResources();
        Messages.Type severity = status == javax.ws.rs.core.Response.Status.OK ? Messages.Type.INFO : Messages.Type.ERROR;
        whoisResources.setErrorMessages(Lists.newArrayList(new ErrorMessage(
                new Message(severity, errorMessage, Collections.emptyList())
        )));
        final javax.ws.rs.core.Response.ResponseBuilder responseBuilder = javax.ws.rs.core.Response.status(status);
        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }
}
