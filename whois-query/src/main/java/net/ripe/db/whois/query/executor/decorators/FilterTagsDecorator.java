package net.ripe.db.whois.query.executor.decorators;

import net.ripe.db.whois.common.collect.IterableTransformer;
import net.ripe.db.whois.common.dao.TagsDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.MessageObject;
import net.ripe.db.whois.query.domain.QueryMessages;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.query.Query;
import net.ripe.db.whois.query.query.QueryFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;
import java.util.Set;

/*
Tagging, merged in one class to avoid multiple DAO lookups for tags, keeping it lightweight
*/

@Component
public class FilterTagsDecorator implements ResponseDecorator {
    private final TagsDao tagsDao;

    @Autowired
    public FilterTagsDecorator(TagsDao tagsDao) {
        this.tagsDao = tagsDao;
    }

    public Iterable<? extends ResponseObject> decorate(final Query query, final Iterable<? extends ResponseObject> input) {
        final boolean showTagInfo = query.hasOption(QueryFlag.SHOW_TAGINFO);
        final boolean hasInclude = query.hasOption(QueryFlag.FILTER_TAG_INCLUDE);
        final boolean hasExclude = query.hasOption(QueryFlag.FILTER_TAG_EXCLUDE);

        if (!(showTagInfo || hasInclude || hasExclude)) {
            return input;
        }

        final Set<CIString> includeArguments = query.getOptionValuesCI(QueryFlag.FILTER_TAG_INCLUDE);
        final Set<CIString> excludeArguments = query.getOptionValuesCI(QueryFlag.FILTER_TAG_EXCLUDE);

        final IterableTransformer<ResponseObject> responseObjects = new IterableTransformer<ResponseObject>(input) {
            @Override
            public void apply(final ResponseObject input, final Deque<ResponseObject> result) {
                if (!(input instanceof RpslObject)) {
                    result.add(input);
                    return;
                }

                final RpslObject object = (RpslObject) input;
                final List<Tag> tags = tagsDao.getTags(object.getObjectId());

                if (hasInclude && !containsTag(tags, includeArguments)) {
                    return;
                }

                if (hasExclude && containsTag(tags, excludeArguments)) {
                    return;
                }

                result.add(object);

                if (showTagInfo) {
                    addTags(object, tags, result);
                }
            }
        };

        if (hasInclude || hasExclude) {
            responseObjects.setHeader(new MessageObject(QueryMessages.filterTagNote(includeArguments, excludeArguments)));
        }

        return responseObjects;
    }

    private void addTags(final RpslObject object, final List<Tag> tags, Deque<ResponseObject> result) {
        if (tags.isEmpty()) {
            return;
        }

        result.add(new MessageObject(QueryMessages.tagInfoStart(object.getKey())));

        for (final Tag tag : tags) {
            result.add(new TagResponseObject(object.getKey(), tag.getType(), tag.getValue()));
        }

        result.add(new MessageObject(""));
    }

    private static boolean containsTag(List<Tag> objectTags, Set<CIString> tagsFromQuery) {
        if (objectTags.isEmpty()) {
            return false;
        }
        for (Tag objectTag : objectTags) {
            if (tagsFromQuery.contains(objectTag.getType())) {
                return true;
            }
        }
        return false;
    }
}
