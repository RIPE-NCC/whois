package net.ripe.db.whois.scheduler.task.export;


import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class TagWriter {

    public void writeTags(final Writer writer, final RpslObject object, final List<Tag> tags) throws IOException {
        if (!tags.isEmpty()) {
            writer.write("\n");
            writer.write(QueryMessages.tagInfoStart(object.getKey()).toString());
            writer.write("\n");

            for (final Tag tag : tags) {
                writer.write(QueryMessages.tagInfo(tag.getType(), tag.getValue()).toString());
                writer.write("\n");
            }
        }
    }
}
