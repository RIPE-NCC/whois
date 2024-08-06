package net.ripe.db.nrtm4;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(value = "classpath:applicationContext-commons.xml")
@ComponentScan(basePackages="net.ripe.db.nrtm4")
public class Nrtmv4Config {
}
