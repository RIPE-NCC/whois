package net.ripe.db.whois.rdap.ipranges.administrative;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile(WhoisProfile.TEST)
public class IanaRangeXmlLoaderConfig {
    final private List<IanaRecord> ianaRecords = new ArrayList<>();

    @Bean
    @Profile(WhoisProfile.TEST)
    public IanaRangeXmlLoader ianaRangeXmlLoader() {

        return new IanaRangeXmlLoader() {

            @PostConstruct
            public void init() {
                try (final InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:IanaAdministrativeRangeTest.xml"))) {
                    final JAXBContext jaxbContext = JAXBContext.newInstance(IanaRegistry.class);
                    final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    final IanaRegistry registry = (IanaRegistry) unmarshaller.unmarshal(in);

                    if (registry.getRecords() == null) {
                        throw new RuntimeException("Records from IANA file is empty");
                    }

                    ianaRecords.addAll(registry.getRecords());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public List<IanaRecord> getIanaRecords() {
                return ianaRecords;
            }
        };
    }
}

