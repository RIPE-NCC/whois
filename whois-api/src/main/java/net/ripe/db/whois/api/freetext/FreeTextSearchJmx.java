package net.ripe.db.whois.api.freetext;

import net.ripe.db.whois.common.jmx.JmxBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.Callable;

@Component
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "FreeTextSearch", description = "Free-text search")
public class FreeTextSearchJmx extends JmxBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchJmx.class);

    private final FreeTextSearch freeTextSearch;
    private final FreeTextIndex freeTextIndex;

    @Autowired
    public FreeTextSearchJmx(final FreeTextSearch freeTextSearch, final FreeTextIndex freeTextIndex) {
        super(LOGGER);
        this.freeTextSearch = freeTextSearch;
        this.freeTextIndex = freeTextIndex;
    }

    @ManagedOperation(description = "Perform free-text search")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "query", description = "Search query")
    })
    public String search(final String query) {
        return invokeOperation("free-text search", query, new Callable<String>() {
            @Override
            public String call() throws IOException {
                final StringWriter writer = new StringWriter();
                freeTextSearch.freeTextSearch(query, writer);
                return writer.toString();
            }
        });
    }

    @ManagedOperation(description = "Free-text index update")
    public String incrementalImport() {
        return invokeOperation("log/update", "", new Callable<String>() {
            @Override
            public String call() {
                freeTextIndex.update();
                return "Free-text index updated";
            }
        });
    }

    @ManagedOperation(description = "Free-text search rebuild")
    public String fullImport() {
        return invokeOperation("rebuild", "", new Callable<String>() {
            @Override
            public String call() {
                freeTextIndex.rebuild();
                return "Free-text index rebuilt";
            }
        });
    }
}
