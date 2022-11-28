package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

@Produces({"text/plain"})
public class CustomMessageBodyWriter implements MessageBodyWriter<WhoisResources> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass == WhoisResources.class;
    }

    @Override
    public long getSize(WhoisResources whoisResources, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MessageBodyWriter.super.getSize(whoisResources, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(WhoisResources whoisResources, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        StringBuilder sb = new StringBuilder();
        for (ErrorMessage errorMessage : whoisResources.getErrorMessages()) {
            sb.append(errorMessage.getSeverity() != null ? "Severity: " + errorMessage.getSeverity() + '\n' : null);
            sb.append(errorMessage.getText() != null ? "Text: " + errorMessage.getText() + '\n' : null);
        }
        sb.append(whoisResources.getTermsAndConditions());
        outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
