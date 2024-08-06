package net.ripe.db.whois.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ComponentScan(basePackages="net.ripe.db.whois.api")
@ImportResource(value = "classpath:applicationContext-api.xml")
public class WhoisApiConfig {
}
