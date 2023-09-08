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

    public GzipOutStreamWriter() throws IOException {
        this.bos = new ByteArrayOutputStream();
        this.gzOut = new GZIPOutputStream(bos);
    }

    public void write(final NrtmFileRecord record) throws IOException {
        gzOut.write(NrtmFileUtil.getNrtmFileRecord(record).getBytes());
    }

    public ByteArrayOutputStream getOutputstream() {
        return this.bos;
    }

    @Override
    protected void finalize() {
        close();
    }

    public void close() {
        try {
            gzOut.close();
        } catch (IOException e) {
            LOGGER.error("Exception while closing gzipStream {}", e);
        }
        try {
            bos.close();
        } catch (IOException e) {
            LOGGER.error("Exception while closing outputstream {}", e);
       }
    }
}
