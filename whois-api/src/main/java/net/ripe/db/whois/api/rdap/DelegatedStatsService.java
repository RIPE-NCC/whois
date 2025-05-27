package net.ripe.db.whois.api.rdap;


import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciSet;

@Component
public class DelegatedStatsService implements EmbeddedValueResolverAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedStatsService.class);

    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Set<ObjectType> ALLOWED_OBJECTTYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INET6NUM, ObjectType.INETNUM);

    private final Map<CIString, String> sourceToPathMap = Maps.newHashMap();
    private final Set<CIString> sources;
    private final AuthoritativeResourceData resourceData;
    private StringValueResolver valueResolver;

    @Autowired
    public DelegatedStatsService(@Value("${rdap.sources:}") final String rdapSourceNames,
                                 final AuthoritativeResourceData resourceData) {
        this.sources = ciSet(COMMA_SPLITTER.split(rdapSourceNames));
        this.resourceData = resourceData;
    }

    @Override
    public void setEmbeddedValueResolver(final StringValueResolver valueResolver) {
        this.valueResolver = valueResolver;
    }

    @PostConstruct
    void init() {
        for (CIString source : sources) {
            final String sourceName = source.toLowerCase().replace("-grs", "");
            final String propertyName = String.format("${rdap.redirect.%s:}", sourceName);
            final String redirectUrl = valueResolver.resolveStringValue(propertyName);
            if (!StringUtils.isBlank(redirectUrl)) {
                sourceToPathMap.put(source, redirectUrl);
            }
        }
    }

    public URI getUriForRedirect(final String requestPath, final Query query) {
        final Optional<ObjectType> objectType = query.getObjectTypes().stream()
            .filter(ALLOWED_OBJECTTYPES::contains)
            .findFirst();

        return getUriForRedirect(requestPath, objectType.orElse(null), query.getSearchValue());
    }

    public URI getUriForRedirect(final String requestPath, @Nullable final ObjectType objectType, final String searchValue) {
        if (objectType != null) {
            for (Map.Entry<CIString, String> entry : sourceToPathMap.entrySet()) {
                final CIString sourceName = entry.getKey();
                final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(sourceName);
                if (authoritativeResource.isMaintainedInRirSpace(objectType, CIString.ciString(searchValue))) {
                    final String basePath = entry.getValue();
                    LOGGER.debug("Redirecting {} to {}", requestPath, sourceName);
                    // TODO: don't include local path prefix (lookup from base context and replace)
                    try {
                        return URI.create(String.format("%s%s", basePath, requestPath.replaceFirst("/rdap", "")));
                    } catch (IllegalArgumentException ex){
                        throw new RdapException("400 Bad Request", "Wrong URL format", HttpStatus.BAD_REQUEST_400);
                    }
                }
            }
        }

        LOGGER.debug("Resource {} not found", searchValue);
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    public boolean isMaintainedInRirSpace(final CIString source, final ObjectType objectType, final CIString pkey) {
        final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(source);
        return authoritativeResource.isMaintainedInRirSpace(objectType, pkey);
    }
}
