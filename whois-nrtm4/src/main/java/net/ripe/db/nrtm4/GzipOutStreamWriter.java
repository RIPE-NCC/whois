package net.ripe.db.nrtm4;

import net.ripe.db.nrtm4.domain.NrtmFileRecord;
import net.ripe.db.nrtm4.util.NrtmFileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GzipOutStreamWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GzipOutStreamWriter.class);

    final ByteArrayOutputStream bos;
    final GZIPOutputStream gzOut;

    public GzipOutStreamWriter() {
        try {
            this.bos = new ByteArrayOutputStream();
            this.gzOut = new GZIPOutputStream(bos);
        } catch (IOException e) {
            LOGGER.warn("Error while creating outputstream", e);
            throw new RuntimeException(e);
        }
    }

    public void write(final NrtmFileRecord record)  {
        try {
            gzOut.write(NrtmFileUtil.convertToJSONTextSeq(record).getBytes());
        } catch (IOException e) {
            LOGGER.warn("Error while writing snapshotfile", e);
            throw new RuntimeException(e);
        }
    }

    public ByteArrayOutputStream getOutputstream() {
        return this.bos;
    }

    public void close() {
        LOGGER.debug("closing resources");
        try {
            gzOut.close();
        } catch (IOException e) {
            LOGGER.debug("Exception while closing gzipStream {}", e);
        }
        try {
            bos.close();
        } catch (IOException e) {
            LOGGER.debug("Exception while closing outputstream {}", e);
       }
    }
}
