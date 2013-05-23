package net.ripe.db.whois.common.grs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LacnicResourceData extends AuthoritativeResourceDataFromUrl {

    @Autowired
    public LacnicResourceData(
            @Value("${grs.import.lacnic.resourceDataUrl:}") final String resourceDataUrl,
            @Value("${grs.import.lacnic.source:}") final String source,
            @Value("${dir.grs.import.download:}") final String downloadDir) {
        super(resourceDataUrl, source, downloadDir);
    }
}
