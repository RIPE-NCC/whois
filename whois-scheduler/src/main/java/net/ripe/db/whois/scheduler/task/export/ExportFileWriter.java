package net.ripe.db.whois.scheduler.task.export;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ExportFileWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportFileWriter.class);

    private final File baseDir;
    private final FilenameStrategy filenameStrategy;
    private final DecorationStrategy decorationStrategy;
    private final ExportFilter exportFilter;
    private final Map<String, Writer> writerMap = Maps.newHashMap();

    public ExportFileWriter(final File baseDir,
                            final FilenameStrategy filenameStrategy,
                            final DecorationStrategy decorationStrategy,
                            final ExportFilter exportFilter) {
        this.baseDir = baseDir;
        this.filenameStrategy = filenameStrategy;
        this.decorationStrategy = decorationStrategy;
        this.exportFilter = exportFilter;

        for (final ObjectType objectType : ObjectType.values()) {
            final String filename = filenameStrategy.getFilename(objectType);
            if (filename != null) {
                try {
                    getWriter(filename);
                } catch (IOException e) {
                    throw new RuntimeException("Initializing: " + filename, e);
                }
            }
        }
    }

    public void write(final RpslObject object) throws IOException {
        if (exportFilter.shouldExport(object)) {
            final String filename = filenameStrategy.getFilename(object.getType());
            if (filename != null) {
                final Writer writer = getWriter(filename);

                final RpslObject decoratedObject = decorationStrategy.decorate(object);
                if (decoratedObject != null) {
                    writer.write('\n');
                    decoratedObject.writeTo(writer);
                }
            }
        }
    }

    public void close() {
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
                writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fileOutputStream), StandardCharsets.ISO_8859_1));
                writer.write(QueryMessages.termsAndConditionsDump().toString());
                writerMap.put(filename, writer);
            } catch (IOException e) {
                fileOutputStream.close();
            }
        }

        return writer;
    }
}
