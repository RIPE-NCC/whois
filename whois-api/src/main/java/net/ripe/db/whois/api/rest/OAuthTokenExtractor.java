package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import net.ripe.db.whois.common.apiKey.ApiKeyUtils;
import net.ripe.db.whois.common.apiKey.OAuthSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthTokenExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthTokenExtractor.class);

    public static OAuthSession extract(final HttpServletRequest request) {
        if(!request.getQueryString().contains(ApiKeyUtils.APIKEY_QUERY_PARAM)) {
            return null;
        }

        try {
            return ApiKeyUtils.getOAuthSession(request.getParameterMap().get(ApiKeyUtils.APIKEY_QUERY_PARAM)[0]);
        } catch (JsonProcessingException e) {
            LOGGER.error("This can never happen");
            return null;
        }
    }
}
