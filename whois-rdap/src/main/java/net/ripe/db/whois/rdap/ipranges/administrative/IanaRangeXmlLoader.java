package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class IanaRangeXmlLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IanaRangeXmlLoader.class);
    final private List<IanaRecord> ianaRecords = new ArrayList<>();

    final private String ipv4Url;
    final private String ipv6Url;

    @Autowired
    public IanaRangeXmlLoader(@Value("${ipv4.adminitrative.range:https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml}") String ipv4Url,
                              @Value("${ipv6.adminitrative.range:https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xml}") String ipv6Url) {
        this.ipv4Url = ipv4Url;
        this.ipv6Url = ipv6Url;
    }

    @PostConstruct
    public void init() {
        loadDataFromXml(ipv4Url);
        loadDataFromXml(ipv6Url);

        LOGGER.info("Loaded Iana Records");
        ianaRecords.forEach(ianaRecord -> LOGGER.info(ianaRecord.getPrefix() + ":" + ianaRecord.getRdap().getServer()));
    }

    private void loadDataFromXml(final String url) {
        try {

            final ResourceLoader resourceLoader = new DefaultResourceLoader();

            try (final InputStream in = resourceLoader.getResource(url).getInputStream()) {
                final JAXBContext jaxbContext = JAXBContext.newInstance(IanaRegistry.class);
                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                final IanaRegistry registry = (IanaRegistry) unmarshaller.unmarshal(in);

                if (registry.getRecords() == null) {
                    throw new RuntimeException("Records from IANA file is empty");
                }

                ianaRecords.addAll(registry.getRecords().stream().filter( ianaRecord -> ianaRecord.getRdap() != null).toList());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load IANA ranges", e);
        }
    }

    public List<IanaRecord> getIanaRecords() {
        return ianaRecords;
    }
}
