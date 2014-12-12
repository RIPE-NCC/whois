package net.ripe.db.whois.api.search;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
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

    public final void rebuild() {
        try {
            updateLock.acquireUninterruptibly();
            logger.info("Rebuilding index {}", indexDir);
            final Stopwatch stopwatch = Stopwatch.createStarted();
            index.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    rebuild(indexWriter, taxonomyWriter);
                }
            });
            logger.info("Rebuilt index {} in {}", indexDir, stopwatch.stop());
        } catch (IOException e) {
            logger.error("Rebuilding index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    public final void update() {
        update(new IndexTemplate.WriteCallback() {
                    @Override
                    public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                        update(indexWriter, taxonomyWriter);
                    }
        });
    }

    public final void update(IndexTemplate.WriteCallback writeCallback) {
        if (!updateLock.tryAcquire()) {
            logger.warn("Indexing in progress, skipping update for {}", indexDir);
            return;
        }

        try {
            index.write(writeCallback);
        } catch (IOException e) {
            logger.error("Updating index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    public void delete(final Query query) {
        if (!updateLock.tryAcquire()) {
            logger.warn("Indexing in progress, skipping delete for {}", query.toString());
            return;
        }

        try {
            index.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    indexWriter.deleteDocuments(query);
                }
            });
        } catch (IOException e) {
            logger.error("Updating index: {}", indexDir, e);
        } finally {
            updateLock.release();
        }
    }

    public <T> T search(IndexTemplate.SearchCallback<T> searchCallback) throws IOException {
        if (index == null) {
            throw new IllegalStateException("Index not found.");
        }

        return index.search(searchCallback);
    }

    protected abstract void update(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException;

    protected abstract void rebuild(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException;
}
