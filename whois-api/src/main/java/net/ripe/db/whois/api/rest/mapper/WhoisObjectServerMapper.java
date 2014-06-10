package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisTag;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WhoisObjectServerMapper {
    private final WhoisObjectMapper whoisObjectMapper;
    private final String baseUrl;

    @Autowired
    public WhoisObjectServerMapper(@Value("${api.rest.baseurl}") final String baseUrl,
                                   final WhoisObjectMapper whoisObjectMapper) {
        this.whoisObjectMapper = whoisObjectMapper;
        this.baseUrl = baseUrl;
    }

    public List<WhoisVersion> mapVersions(final List<DeletedVersionResponseObject> deleted, final List<VersionResponseObject> versions) {
        final List<WhoisVersion> whoisVersions = Lists.newArrayList();
        for (final DeletedVersionResponseObject deletedVersion : deleted) {
            whoisVersions.add(new WhoisVersion(deletedVersion.getDeletedDate().toString()));
        }

        for (final VersionResponseObject version : versions) {
            whoisVersions.add(new WhoisVersion(version.getOperation() == Operation.UPDATE ? "ADD/UPD" : "DEL", version.getDateTime().toString(),
                    version.getVersion()));
        }

        return whoisVersions;
    }

    public WhoisObject map(final RpslObject rpslObject, final TagResponseObject tagResponseObject, final Class<?> mapFunction) {
        final WhoisObject object = whoisObjectMapper.map(rpslObject, mapFunction);

        if (tagResponseObject != null && !tagResponseObject.getTags().isEmpty()) {
            final List<Tag> tags = tagResponseObject.getTags();
            final List<WhoisTag> whoisTags = Lists.newArrayListWithExpectedSize(tags.size());
            for (final Tag tag : tags) {
                whoisTags.add(new WhoisTag(tag.getType().toString(), tag.getValue()));
            }
            object.setTags(whoisTags);
        }
        return object;
    }

    public List<WhoisVersionInternal> mapVersionsInternal(final List<VersionResponseObject> versions, final String source, final String type, final String key) {
        final List<WhoisVersionInternal> whoisVersions = Lists.newArrayList();
        for (int i = 0; i < versions.size(); i++) {
            final VersionResponseObject currentVersion = versions.get(i);
            VersionResponseObject nextVersion = null;
            if (i + 1 < versions.size()) {
                nextVersion = versions.get(i + 1);
            }

            if (currentVersion.getOperation() != Operation.DELETE) {
                int versionId = i + 1;
                whoisVersions.add(new WhoisVersionInternal(
                        versionId,
                        currentVersion.getDateTime().toString(),
                        nextVersion == null ? "" : nextVersion.getDateTime().toString(),
                        currentVersion.getOperation().toString(),
                        createWhoisVersionInternalLink(source, type, key + "/" + versionId)));
            }
        }
        return whoisVersions;
    }

    private Link createWhoisVersionInternalLink(String source, String type, String versionId) {
        return new Link("locator", String.format("%s/api/rnd/%s/%s/%s", baseUrl, source, type, versionId));
    }
}
