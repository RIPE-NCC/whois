package net.ripe.db.whois.common.dao;


import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Tag;

import java.util.List;

public interface TagsDao {

    List<Tag> getTags(Integer objectId);

    List<Tag> getTagsOfType(CIString type);

    void createTag(Tag tag);

    void deleteTag(CIString type, Integer objectId);

    void deleteTagsOfType(CIString type);

    void rebuild(CIString type, List<Tag> tags);

    void createTags(List<Tag> tags);

    void deleteTags(CIString type, List<Integer> objectIds);

    void updateTags(Iterable<CIString> tagType, List<Integer> deletes, List<Tag> creates);
}
