package net.ripe.db.whois.api.whois.rdap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.RdapObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.query.domain.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Component
public class RdapExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdapExceptionMapper.class);

    private final RdapObjectMapper rdapObjectMapper;

    @Autowired
    public RdapExceptionMapper(
            final NoticeFactory noticeFactory,
            @Value("${rdap.port43:}") final String port43) {
        this.rdapObjectMapper = new RdapObjectMapper(noticeFactory, port43);
    }


    @Override
    public Response toResponse(final Exception exception) {

        if (exception instanceof IllegalSourceException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(400, exception.getMessage())).build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(400, exception.getMessage())).build();
        }

        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();     // TODO
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(400, exception.getMessage())).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(createErrorEntity(404, "not found")).build();
        }

        if (exception instanceof QueryException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(400, exception.getMessage())).build();
        }

        LOGGER.error("Unexpected", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorEntity(500, exception.getMessage())).build();
    }

    private RdapObject createErrorEntity(final int errorCode, final String errorTitle, String ... errorTexts) {
        return rdapObjectMapper.mapError(errorCode, errorTitle, Lists.newArrayList(errorTexts));
    }
}