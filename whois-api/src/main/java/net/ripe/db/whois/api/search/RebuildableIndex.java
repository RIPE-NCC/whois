package net.ripe.db.whois.api.search;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public abstract class RebuildableIndex {
    private final Logger logger;

    private final Semaphore updateLock = new Semaphore(1);

    protected final String indexDir;
    protected IndexTemplate index;

    protected RebuildableIndex(final Logger logger, final String indexDir) {
        this.logger = logger;
        this.indexDir = indexDir;
    }

    protected void init(final IndexWriterConfig config, final IndexTemplate.WriteCallback initializer) {
        if (!updateLock.tryAcquire()) {
            throw new IllegalStateException("Unable to acquire update lock");
        }

        try {
            index = new IndexTemplate(indexDir, config);
            index.write(initializer);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Initializing index in %s", indexDir), e);
        } finally {
            updateLock.release();
        }
    }

    protected void cleanup() {
        IOUtils.closeQuietly(index);
    }

    public void rebuild() {
        try {
            updateLock.acquireUninterruptibly();
            lockedRebuild();
        } catch (IOException e) {
            logger.error("Rebuilding index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    protected void lockedRebuild() throws IOException {
        logger.info("Rebuilding index {}", indexDir);
        final Stopwatch stopwatch = new Stopwatch().start();
        index.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                rebuild(indexWriter, taxonomyWriter);
            }
        });
        logger.info("Rebuilt index {} in {}", indexDir, stopwatch.stop());
    }

    protected void rebuild(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
    }

    public void update() {
        if (!updateLock.tryAcquire()) {
            logger.warn("Indexing in progress, skipping update for {}", indexDir);
            return;
        }

        try {
            lockedUpdate();
        } catch (IOException e) {
            logger.error("Updating index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    protected void lockedUpdate() throws IOException {
        index.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                update(indexWriter, taxonomyWriter);
            }
        });
    }

    protected void update(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
    }

    public <T> T search(IndexTemplate.SearchCallback<T> searchCallback) throws IOException {
        return index.search(searchCallback);
    }

    public void partialRebuild() {
        if (!updateLock.tryAcquire()) {
            logger.warn("Indexing in progress, skipping update for {}", indexDir);
            return;
        }

        try {
            lockedPartialRebuild();
        } catch (IOException e) {
            logger.error("Updating index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    protected void lockedPartialRebuild() throws IOException {
    }
}
