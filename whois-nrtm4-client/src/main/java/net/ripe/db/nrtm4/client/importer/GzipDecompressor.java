package net.ripe.db.nrtm4.client.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class GzipDecompressor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GzipDecompressor.class);

    private static final int BATCH_SIZE = 10000;

    private static final int BUFFER_SIZE = 4096;

    public static void decompressRecords(final byte[] compressed, Consumer<String> firstRecordProcessor,
                                                   Consumer<String[]> recordBatches){
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream, BUFFER_SIZE);
             InputStreamReader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            String[] batch = new String[BATCH_SIZE];
            int batchIndex = 0;
            boolean isFirstRecord = true;

            while ((line = bufferedReader.readLine()) != null) {
                final String record = line.trim(); //each line is one record

                if (record.isEmpty()) {
                    continue;
                }
                if (isFirstRecord) {
                    firstRecordProcessor.accept(record);  // Process the first record
                    isFirstRecord = false;
                } else {
                    batch[batchIndex++] = record;
                    if (batchIndex == BATCH_SIZE) {
                        recordBatches.accept(batch);
                        batch = new String[BATCH_SIZE];
                        batchIndex = 0;
                    }
                }
            }
            if (batchIndex > 0) {
                recordBatches.accept(Arrays.copyOf(batch, batchIndex));
            }

        } catch (IOException e) {
            LOGGER.error("Error when decompressing the snapshot", e);
            throw new IllegalStateException(e);
        }
    }
}
