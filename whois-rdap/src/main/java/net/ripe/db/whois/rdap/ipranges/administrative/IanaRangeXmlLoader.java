package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile(WhoisProfile.DEPLOYED)
public class IanaRangeXmlLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IanaRangeXmlLoader.class);
    final private List<IanaRecord> ianaRecords = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadDataFromXml("https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml");
        loadDataFromXml("https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xml");
    }

    private void loadDataFromXml(final String url) {
        try {

            try (final InputStream in = URI.create(url).toURL().openStream()) {
                final JAXBContext jaxbContext = JAXBContext.newInstance(IanaRegistry.class);
                final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                final IanaRegistry registry = (IanaRegistry) unmarshaller.unmarshal(in);

                if (registry.getRecords() == null) {
                    throw new RuntimeException("Records from IANA file is empty");
                }

                ianaRecords.addAll(registry.getRecords().stream().filter( ianaRecord -> ianaRecord.getRdap() != null).toList());

                LOGGER.info("Loaded Iana Records");
                ianaRecords.forEach(ianaRecord -> LOGGER.info(ianaRecord.getPrefix() + ":" + ianaRecord.getRdap().getServer()));
            }

        } catch (Exception e) {
            LOGGER.error("Failed to load IANA ranges", e);
        }
    }

    public List<IanaRecord> getIanaRecords() {
        return ianaRecords;
    }
}
