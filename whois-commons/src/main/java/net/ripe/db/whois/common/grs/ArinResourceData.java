package net.ripe.db.whois.common.grs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ArinResourceData extends AuthoritativeResourceDataFromUrl {

    @Autowired
    public ArinResourceData(
            @Value("${grs.import.arin.resourceDataUrl:}") final String resourceDataUrl,
            @Value("${grs.import.arin.source:}") final String source,
            @Value("${dir.grs.import.download:}") final String downloadDir) {
        super(resourceDataUrl, source, downloadDir);
    }
}
