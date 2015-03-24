package net.ripe.db.whois.scheduler.task.export;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.TagResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

class ExportFileWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportFileWriter.class);

    private final File baseDir;
    private final FilenameStrategy filenameStrategy;
    private final DecorationStrategy decorationStrategy;
    private final Map<String, Writer> writerMap = Maps.newHashMap();
    // TODO: remove once timestamps are always on (MG)
    private final boolean timestampsOff;

    protected ExportFileWriter(final File baseDir, final FilenameStrategy filenameStrategy, final DecorationStrategy decorationStrategy, final boolean timestampsOff) {
        this.baseDir = baseDir;
        this.filenameStrategy = filenameStrategy;
        this.decorationStrategy = decorationStrategy;
        this.timestampsOff = timestampsOff;

        for (final ObjectType objectType : ObjectType.values()) {
            final String filename = filenameStrategy.getFilename(objectType);
            try {
                getWriter(filename);
            } catch (IOException e) {
                throw new RuntimeException("Initializing: " + filename, e);
            }
        }
    }

    void write(final RpslObject object, final List<Tag> tags) throws IOException {
        final String filename = filenameStrategy.getFilename(object.getType());
        final Writer writer = getWriter(filename);

        RpslObject decoratedObject = decorationStrategy.decorate(object);
        if (decoratedObject != null) {
            // TODO: remove once timestamps are always on (MG)
            RpslObject strippedDecoratedObject = decoratedObject;
            if( timestampsOff == true ) {
                strippedDecoratedObject = stripTimestampAttributes(decoratedObject);
            }
            writer.write('\n');
            strippedDecoratedObject.writeTo(writer);

            if (!tags.isEmpty()) {
                writer.write('\n');
                writer.write(new TagResponseObject(decoratedObject.getKey(), tags).toString());
            }
        }
    }

    private RpslObject stripTimestampAttributes(final RpslObject object) {
        RpslObjectBuilder builder = new RpslObjectBuilder(object);
        if (object.containsAttribute(AttributeType.LAST_MODIFIED)) {
            builder.removeAttributeType(AttributeType.LAST_MODIFIED);
        }
        if (object.containsAttribute(AttributeType.CREATED)) {
            builder.removeAttributeType(AttributeType.CREATED);
        }
        return builder.get();
    }

    void close() {
        for (final Map.Entry<String, Writer> entry : writerMap.entrySet()) {
            final Writer writer = entry.getValue();
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                LOGGER.error("Closing {}/{}", baseDir, entry.getKey());
            }
        }
    }

    private Writer getWriter(final String filename) throws IOException {
        Writer writer = writerMap.get(filename);
        if (writer == null) {
            final File file = new File(baseDir, filename + ".gz");
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream), Charsets.ISO_8859_1));
                writer.write(QueryMessages.termsAndConditionsDump().toString());
                writerMap.put(filename, writer);
            } catch (IOException e) {
                fileOutputStream.close();
            }
        }

        return writer;
    }
}
