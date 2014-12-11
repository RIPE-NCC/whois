package net.ripe.db.whois.api.whois.rdap;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.query.Query;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciSet;

@Component
public class DelegatedStatsService implements EmbeddedValueResolverAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedStatsService.class);

    private static final Set<ObjectType> ALLOWED_OBJECTTYPES = Sets.newHashSet(ObjectType.AUT_NUM, ObjectType.INET6NUM, ObjectType.INETNUM);
    private final Map<CIString, String> sourceToPathMap = Maps.newHashMap();
    private final Set<CIString> sources;
    private final AuthoritativeResourceData resourceData;
    private StringValueResolver valueResolver;

    @Autowired
    public DelegatedStatsService(@Value("${rdap.sources:}") String rdapSourceNames,
                                 final AuthoritativeResourceData resourceData) {
        this.sources = ciSet(Splitter.on(',').split(rdapSourceNames));
        this.resourceData = resourceData;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver valueResolver) {
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
        final Optional<ObjectType> objectType = Iterables.tryFind(query.getObjectTypes(), new Predicate<ObjectType>() {
            @Override
            public boolean apply(ObjectType input) {
                return ALLOWED_OBJECTTYPES.contains(input);
            }
        });

        if (objectType.isPresent()) {
            for (Map.Entry<CIString, String> entry : sourceToPathMap.entrySet()) {
                final CIString sourceName = entry.getKey();
                final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(sourceName);
                LOGGER.info("trying authoritativeResource for source {}", sourceName);
                if (authoritativeResource.isMaintainedInRirSpace(objectType.get(), CIString.ciString(query.getSearchValue()))) {
                    final String basePath = entry.getValue();
                    LOGGER.debug("Redirecting {} to {}", requestPath, sourceName);
                    // TODO: don't include local path prefix (lookup from base context and replace)
                    return URI.create(String.format("%s%s", basePath, requestPath.replaceFirst("/rdap", "")));
                }
            }
        }

        LOGGER.info("Resource {} not found", query.getSearchValue());
        LOGGER.debug("Resource {} not found", query.getSearchValue());
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    public boolean isMaintainedInRirSpace(final CIString source, final ObjectType objectType, final CIString pkey){
        final AuthoritativeResource authoritativeResource = resourceData.getAuthoritativeResource(source);
        return authoritativeResource.isMaintainedInRirSpace(objectType, pkey);
    }
}
