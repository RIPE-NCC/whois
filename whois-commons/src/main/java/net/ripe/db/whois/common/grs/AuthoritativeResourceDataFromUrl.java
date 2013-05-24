package net.ripe.db.whois.common.grs;

import net.ripe.db.whois.common.io.Downloader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;

abstract class AuthoritativeResourceDataFromUrl implements AuthoritativeResourceData {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeResourceDataFromUrl.class);

    private final String resourceDataUrl;
    private final String source;
    private final String downloadDir;

    private AuthoritativeResource authoritativeResource;

    AuthoritativeResourceDataFromUrl(final String resourceDataUrl, final String source, final String downloadDir) {
        this.resourceDataUrl = resourceDataUrl;
        this.source = source;
        this.downloadDir = downloadDir;
    }

    // TODO [AK] Refresh data periodically
    // TODO [AK] We really don't want to download all these files on application startup
    @PostConstruct
    private void initResourceData() throws IOException {
        if (StringUtils.isBlank(resourceDataUrl)) {
            authoritativeResource = AuthoritativeResource.unknown(LOGGER);
        } else {
            final File resourceDataFile = new File(downloadDir, source + "-RES");
            Downloader.downloadGrsData(LOGGER, new URL(resourceDataUrl), resourceDataFile);
            authoritativeResource = AuthoritativeResource.loadFromFile(LOGGER, source, resourceDataFile);
        }
    }

    @Override
    public AuthoritativeResource getAuthoritativeResource() {
        return authoritativeResource;
    }
}
