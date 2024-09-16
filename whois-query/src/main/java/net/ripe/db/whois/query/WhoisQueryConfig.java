package net.ripe.db.whois.query;

import net.ripe.db.whois.common.WhoisCommonConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(WhoisCommonConfig.class)
@ComponentScan(basePackages = "net.ripe.db.whois.query")
public class WhoisQueryConfig {
}
