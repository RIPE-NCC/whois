package net.ripe.db.nrtm4;

import net.ripe.db.whois.common.WhoisCommonConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(WhoisCommonConfig.class)
@ComponentScan(basePackages="net.ripe.db.nrtm4")
public class Nrtmv4Config {
}
