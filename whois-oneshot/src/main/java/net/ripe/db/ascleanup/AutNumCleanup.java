package net.ripe.db.ascleanup;

import com.google.common.collect.Sets;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceLoader;
import net.ripe.db.whois.common.io.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class AutNumCleanup {
    public static void main(String[] argv) throws Exception {
        final Logger logger = LoggerFactory.getLogger("AutNumCleanup");
        final Path resourceDataFile = Files.createTempFile("AutNumCleanup", "");
        final Downloader downloader = new Downloader();

        downloader.downloadGrsData(logger, new URL("ftp://ftp.ripe.net/ripe/stats/delegated-ripencc-extended-latest"), resourceDataFile);

        final AuthoritativeResourceLoader authoritativeResourceLoader = new AuthoritativeResourceLoader(logger, "ripe", new Scanner(resourceDataFile), Sets.newHashSet("available", "reserved"));
        final AuthoritativeResource authoritativeResource = authoritativeResourceLoader.load();
        final List<String> autNumResources = authoritativeResource.getAutNumResources();

        

    }

}
