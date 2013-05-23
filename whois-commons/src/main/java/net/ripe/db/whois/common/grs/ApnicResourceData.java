package net.ripe.db.whois.common.grs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApnicResourceData extends AuthoritativeResourceDataFromUrl {

    @Autowired
    public ApnicResourceData(
            @Value("${grs.import.apnic.resourceDataUrl:}") final String resourceDataUrl,
            @Value("${grs.import.apnic.source:}") final String source,
            @Value("${dir.grs.import.download:}") final String downloadDir) throws IOException {
        super(resourceDataUrl, source, downloadDir);
    }
}
