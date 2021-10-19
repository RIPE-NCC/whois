package net.ripe.db.whois.api.fulltextsearch;

import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.acl.AccessControlListManager;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.taxonomy.FacetLabel;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.ReaderManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
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
            taxonomy = FSDirectory.open(new File(directory, "taxonomy").toPath());
            index = FSDirectory.open(new File(directory, "index").toPath());
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
    public void close() {
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

        config = new IndexWriterConfig(config.getAnalyzer());
        indexWriter = new IndexWriter(index, config);

        taxonomyWriter.commit();
        indexWriter.commit();

        readerManager = new ReaderManager(indexWriter, false, false);
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

    public abstract static class AccountingSearchCallback<T> implements SearchCallback<T> {

        private static final Set<String> SEARCH_INDEX_FIELDS_NOT_MAPPED_TO_RPSL_OBJECT = Sets.newHashSet("primary-key", "object-type", "lookup-key");

        private final AccessControlListManager accessControlListManager;
        private final InetAddress remoteAddress;
        private final Source source;

        private int accountingLimit = -1;
        private int accountedObjects = 0;

        private final boolean shouldDoAccounting;

        public AccountingSearchCallback(final AccessControlListManager accessControlListManager,
                                        final String remoteAddress,
                                        final Source source) {
            this.accessControlListManager = accessControlListManager;
            this.remoteAddress = InetAddresses.forString(remoteAddress);
            this.shouldDoAccounting = !accessControlListManager.isUnlimited(this.remoteAddress);
            this.source = source;
        }

        public T search(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException {
            if (accessControlListManager.isDenied(remoteAddress)) {
                throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedPermanently(remoteAddress));
            } else if (!accessControlListManager.canQueryPersonalObjects(remoteAddress)) {
                throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
            }

            try {
                return doSearch(indexReader, taxonomyReader, indexSearcher);
            } finally {
                if (shouldDoAccounting && accountedObjects > 0) {
                    accessControlListManager.accountPersonalObjects(remoteAddress, accountedObjects);
                }
            }
        }

        protected abstract T doSearch(IndexReader indexReader, TaxonomyReader taxonomyReader, IndexSearcher indexSearcher) throws IOException;

        protected void account(final RpslObject rpslObject) {
            if (shouldDoAccounting && accessControlListManager.requiresAcl(rpslObject, source)) {
                if (accountingLimit == -1) {
                    accountingLimit = accessControlListManager.getPersonalObjects(remoteAddress);
                }

                if (++accountedObjects > accountingLimit) {
                    throw new QueryException(QueryCompletionInfo.BLOCKED, QueryMessages.accessDeniedTemporarily(remoteAddress));
                }
            }
        }

        protected int getObjectId(Document document) {
            for (final IndexableField field : document.getFields()) {
                if (SEARCH_INDEX_FIELDS_NOT_MAPPED_TO_RPSL_OBJECT.contains(field.name())) {
                    if ("primary-key".equals(field.name())) {
                        return  Integer.parseInt(field.stringValue());
                    }
                }
            }

           throw new IllegalStateException("luecene index should always have a primary key stored");
        }
    }

}
