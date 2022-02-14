package net.ripe.db.whois.api.elasticsearch;

import org.slf4j.Logger;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import static org.slf4j.LoggerFactory.getLogger;

public class ElasticsearchDisableableContainer extends ElasticsearchContainer {

    private static final Logger LOGGER = getLogger(ElasticsearchDisableableContainer.class);

    private boolean isActive;

    public ElasticsearchDisableableContainer(final String dockerImageName) {
        super(dockerImageName);
    }

    @Override
    public void start() {
        if (isActive) {
            super.start();
        }
        LOGGER.info("Container started at port {}", this.getHttpHostAddress());
    }

    public ElasticsearchDisableableContainer isActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
}