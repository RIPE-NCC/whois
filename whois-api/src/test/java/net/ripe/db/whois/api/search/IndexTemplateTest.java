package net.ripe.db.whois.api.search;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.TaxonomyWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class IndexTemplateTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();
    IndexTemplate subject;
    WhitespaceAnalyzer analyzer;

    @Before
    public void setUp() throws Exception {
        analyzer = new WhitespaceAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_10_0, analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        subject = new IndexTemplate(folder.getRoot().getAbsolutePath(), config);
    }

    @After
    public void tearDown() throws Exception {
        subject.close();
    }

    @Test
    public void index_and_search() throws IOException, ParseException {
        subject.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                addDoc(indexWriter, "Lucene in Action", "193398817");
                addDoc(indexWriter, "Lucene for Dummies", "55320055Z");
                addDoc(indexWriter, "Managing Gigabytes", "55063554A");
                addDoc(indexWriter, "The Art of Computer Science", "9900333X");

                assertThat(indexWriter.numDocs(), is(4));
            }
        });

        final Query query = new QueryParser("title", analyzer).parse("Lucene");
        subject.search(new IndexTemplate.SearchCallback<Void>() {
            @Override
            public Void search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                final TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
                indexSearcher.search(query, collector);
                final ScoreDoc[] hits = collector.topDocs().scoreDocs;

                assertThat(hits.length, is(2));
                assertThat(indexReader.document(hits[0].doc).get("isbn"), is("193398817"));
                assertThat(indexReader.document(hits[1].doc).get("isbn"), is("55320055Z"));
                return null;
            }
        });
    }

    @Test
    public void index_concurrent() throws Exception {
        final int nrThreads = 4;
        final CountDownLatch countDownLatch = new CountDownLatch(nrThreads);
        final ExecutorService executorService = Executors.newFixedThreadPool(nrThreads);

        assertThat(numDocs(), is(0));

        for (int i = 0; i < nrThreads; i++) {
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        subject.write(new IndexTemplate.WriteCallback() {
                            @Override
                            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                                addDoc(indexWriter, toString(), toString());
                            }
                        });
                    } finally {
                        countDownLatch.countDown();
                    }

                    return null;
                }
            });
        }

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertThat(numDocs(), is(4));
    }

    @Test
    public void search_concurrent() throws Exception {
        final int nrDocs = 1000;
        final int nrThreads = 100;

        subject.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                for (int i = 0; i < nrDocs; i++) {
                    addDoc(indexWriter, "title: " + i, "isbn: " + i);
                }

                assertThat(indexWriter.numDocs(), is(nrDocs));
            }
        });

        final CountDownLatch countDownLatch = new CountDownLatch(nrThreads);
        final ExecutorService executorService = Executors.newFixedThreadPool(nrThreads);
        final Query query = new QueryParser("title", analyzer).parse("title");
        for (int i = 0; i < nrThreads; i++) {
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        subject.search(new IndexTemplate.SearchCallback<Void>() {
                            @Override
                            public Void search(final IndexReader indexReader, final TaxonomyReader taxonomyReader, final IndexSearcher indexSearcher) throws IOException {
                                final TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
                                indexSearcher.search(query, collector);
                                assertThat(collector.topDocs().scoreDocs.length, is(nrDocs));
                                return null;
                            }
                        });
                    } finally {
                        countDownLatch.countDown();
                    }

                    return null;
                }
            });
        }

        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void out_of_memory() throws IOException {
        try {
            subject.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    addDoc(indexWriter, "title", "isbn");
                    throw new OutOfMemoryError();
                }
            });

            fail("Expected exception");
        } catch (OutOfMemoryError ignored) {
        }

        // no rollback on out of memory, document was written properly
        assertThat(numDocs(), is(1));

        subject.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                addDoc(indexWriter, "title", "isbn");
            }
        });

        assertThat(numDocs(), is(2));
    }

    @Test
    public void runtime_exception() throws IOException {
        try {
            subject.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    addDoc(indexWriter, "title", "isbn");
                    throw new IllegalStateException();
                }
            });

            fail("Expected exception");
        } catch (IllegalStateException ignored) {
        }

        assertThat(numDocs(), is(0));

        subject.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                addDoc(indexWriter, "title", "isbn");
            }
        });

        assertThat(numDocs(), is(1));
    }

    @Test
    public void io_exception() throws IOException {
        try {
            subject.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    addDoc(indexWriter, "title", "isbn");
                    throw new IOException();
                }
            });

            fail("Expected exception");
        } catch (IOException ignored) {
        }

        assertThat(numDocs(), is(0));

        subject.write(new IndexTemplate.WriteCallback() {
            @Override
            public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                addDoc(indexWriter, "title", "isbn");
            }
        });

        assertThat(numDocs(), is(1));
    }

    @Test
    public void refresh() throws IOException {
        assertThat(numDocs(), is(0));

        for (int i = 1; i <= 10; i++) {
            subject.write(new IndexTemplate.WriteCallback() {
                @Override
                public void write(final IndexWriter indexWriter, final TaxonomyWriter taxonomyWriter) throws IOException {
                    addDoc(indexWriter, "title", String.valueOf("isbn"));
                }
            });

            assertThat(numDocs(), is(i));
        }
    }

    int numDocs() throws IOException {
        return subject.read(new IndexTemplate.ReadCallback<Integer>() {
            @Override
            public Integer read(final IndexReader indexReader, final TaxonomyReader taxonomyReader) throws IOException {
                return indexReader.numDocs();
            }
        });
    }

    void addDoc(final IndexWriter indexWriter, final String title, final String isbn) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        indexWriter.addDocument(doc);
    }
}
