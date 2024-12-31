package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class BearerTokenExtractor   {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenExtractor.class);

    public static OAuthSession extractBearerToken(final HttpServletRequest request, final String accessKey) {
        if(StringUtils.isEmpty(accessKey)) {
            return null;
        }

        final String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        return getOAuthSession(bearerToken, accessKey);
    }


    public static OAuthSession getOAuthSession(final String bearerToken, final String accessKey) {
        if(StringUtils.isEmpty(bearerToken)) {
            return new OAuthSession(accessKey);
        }

        try {
            final String payload = new String(Base64.getUrlDecoder().decode(StringUtils.substringAfter(bearerToken, "Bearer ").split("\\.")[1]));
            if(StringUtils.isEmpty(payload)) {
                throw new RuntimeException("Bearer token is missing or invalid");
            }

            //TODO: remove when accessKey is available from api registry call
            return OAuthSession.from(new ObjectMapper().readValue(payload, OAuthSession.class), accessKey);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize OAuthSession, this should never have happened", e);
            return  new OAuthSession(accessKey);
        } catch (Exception e) {
            LOGGER.error("Failed to read OAuthSession, this should never have happened", e);
            return new OAuthSession(accessKey);
        }
    }
}
