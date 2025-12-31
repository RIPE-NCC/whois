package net.ripe.db.whois.query;

import net.ripe.db.whois.common.configuration.WhoisCommonConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WhoisCommonConfiguration.class)
@ComponentScan(basePackages = "net.ripe.db.whois.query")
public class WhoisQueryConfiguration {
}
