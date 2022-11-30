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
public class WhoisResourcesPlainTextWriter implements MessageBodyWriter<WhoisResources> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return aClass == WhoisResources.class && MediaType.TEXT_PLAIN.contains(mediaType.getType());
    }

    @Override
    public long getSize(WhoisResources whoisResources, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MessageBodyWriter.super.getSize(whoisResources, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(WhoisResources whoisResources, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        StringBuilder sb = new StringBuilder();
        for (ErrorMessage errorMessage : whoisResources.getErrorMessages()) {
            if (errorMessage.getSeverity() != null){
                sb.append("Severity: ").append(errorMessage.getSeverity()).append('\n');
            }
            if (errorMessage.getText() != null){
                sb.append("Text: ").append(errorMessage.getText()).append('\n');
            }
        }
        sb.append(whoisResources.getTermsAndConditions());
        outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
