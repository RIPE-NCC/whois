package net.ripe.db.whois.common.grs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// TODO [AK] Add single component to manage all resource data
@Component
public class AfrinicResourceData extends AuthoritativeResourceDataFromUrl {

    @Autowired
    public AfrinicResourceData(
            @Value("${grs.import.afrinic.resourceDataUrl:}") final String resourceDataUrl,
            @Value("${grs.import.afrinic.source:}") final String source,
            @Value("${dir.grs.import.download:}") final String downloadDir) {
        super(resourceDataUrl, source, downloadDir);
    }
}
