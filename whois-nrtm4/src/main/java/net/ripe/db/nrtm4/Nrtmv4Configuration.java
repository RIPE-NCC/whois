package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.configuration.WhoisCommonConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WhoisCommonConfiguration.class)
@ComponentScan(basePackages="net.ripe.db.nrtm4")
public class Nrtmv4Configuration {
}
