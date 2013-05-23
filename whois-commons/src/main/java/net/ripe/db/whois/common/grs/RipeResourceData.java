package net.ripe.db.whois.common.grs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RipeResourceData extends AuthoritativeResourceDataFromUrl {

    @Autowired
    public RipeResourceData(
            @Value("${grs.import.ripe.resourceDataUrl:}") final String resourceDataUrl,
            @Value("${grs.import.ripe.source:}") final String source,
            @Value("${dir.grs.import.download:}") final String downloadDir) {
        super(resourceDataUrl, source, downloadDir);
    }
}
