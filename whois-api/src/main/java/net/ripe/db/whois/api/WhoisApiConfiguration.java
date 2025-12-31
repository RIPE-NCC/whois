package net.ripe.db.whois.api;

import net.ripe.db.whois.api.fulltextsearch.SearchResponse;
import net.ripe.db.whois.query.WhoisQueryConfiguration;
import net.ripe.db.whois.update.WhoisUpdateConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@ComponentScan(basePackages= "net.ripe.db.whois.api")
@Import({WhoisQueryConfiguration.class, WhoisUpdateConfiguration.class})
public class WhoisApiConfiguration {

    @Bean("marshaller")
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(SearchResponse.class);
        return marshaller;
    }
}
