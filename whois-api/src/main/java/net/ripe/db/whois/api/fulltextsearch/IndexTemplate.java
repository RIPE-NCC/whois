package net.ripe.db.whois.api.fulltextsearch;

import net.ripe.db.whois.common.rpsl.ObjectType;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import static org.apache.lucene.util.IOUtils.closeWhileHandlingException;

public class IndexTemplate implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexTemplate.class);

    private final Directory taxonomy;
    private final Directory index;
    private final Semaphore updateLock = new Semaphore(1);

    private IndexWriter indexWriter;
    private ReaderManager readerManager;
    private DirectoryTaxonomyWriter taxonomyWriter;
    private IndexWriterConfig config;

    public IndexTemplate(final String directory, final IndexWriterConfig config) throws IOException {
        if (StringUtils.isEmpty(directory)) {
            LOGGER.warn("Using RAM directory for index");
            taxonomy = new RAMDirectory();
            index = new RAMDirectory();
        } else {
            LOGGER.info("Using index directory: {}", directory);
            taxonomy = FSDirectory.open(new File(directory, "taxonomy"));
            index = FSDirectory.open(new File(directory, "index"));
        }

        this.config = config;

        updateLock.acquireUninterruptibly();

        try {
            createNewWriters();
        } finally {
            updateLock.release();
        }
    }

    @Override
    public void close() throws IOException {
        updateLock.acquireUninterruptibly();

        try {
            closeWhileHandlingException(readerManager, indexWriter, taxonomyWriter, index, taxonomy);
        } finally {
            updateLock.release();
        }
    }

    public void write(final WriteCallback writeCallback) throws IOException {
        updateLock.acquireUninterruptibly();

        try {
            writeCallback.write(indexWriter, taxonomyWriter);
            taxonomyWriter.prepareCommit();
            indexWriter.prepareCommit();

            taxonomyWriter.commit();
            indexWriter.commit();

            readerManager.maybeRefresh();
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Unexpected", e);
            rollback();
            throw e;
        } catch (OutOfMemoryError e) {
            LOGGER.error("Unexpected", e);
            createNewWriters();
            throw e;
        } finally {
            updateLock.release();
        }
    }

    private void rollback() throws IOException {
        try {
            indexWriter.rollback();
            taxonomyWriter.rollback();
        } finally {
            createNewWriters();
        }
    }

    private void createNewWriters() throws IOException {
        closeWhileHandlingException(taxonomyWriter, indexWriter);
        taxonomyWriter = new DirectoryTaxonomyWriter(taxonomy);
        addFacetCategories(taxonomyWriter);

        config = new IndexWriterConfig(Version.LUCENE_4_10_4, config.getAnalyzer());
        indexWriter = new IndexWriter(index, config);

        taxonomyWriter.commit();
        indexWriter.commit();

        readerManager = new ReaderManager(indexWriter, true);
    }

    private static void addFacetCategories(final TaxonomyWriter taxonomyWriter) throws IOException {
        for (ObjectType objectType : ObjectType.values()) {
            final FacetLabel facetLabel = new FacetLabel("object-type", objectType.getName());
            taxonomyWriter.addCategory(facetLabel);
        }
    }

    public <T> T read(final ReadCallback<T> readCallback) throws IOException {
        DirectoryReader indexReader = null;
        TaxonomyReader taxonomyReader = null;

        try {
            indexReader = readerManager.acquire();
            taxonomyReader = new DirectoryTaxonomyReader(taxonomy);

            return readCallback.read(indexReader, taxonomyReader);
        } finally {
            closeWhileHandlingException(taxonomyReader);
            readerManager.release(indexReader);
        }
    }

    public <T> T search(final SearchCallback<T> searchCallback) throws IOException {
        return read((final IndexReader indexReader, final TaxonomyReader taxonomyReader) -> {
            final IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            return searchCallback.search(indexReader, taxonomyReader, indexSearcher);
        });
    }

    public interface WriteCallback {
        void write(IndexWriter indexWriter, TaxonomyWriter taxonomyWriter) throws IOException;
    }

    public interface ReadCallback<T> {
        T read(IndexReader indexReader, TaxonomyReader taxonomyReader) throws IOException;
    }

    public interface SearchCallback<T> {
        T search(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException;
    }
}
