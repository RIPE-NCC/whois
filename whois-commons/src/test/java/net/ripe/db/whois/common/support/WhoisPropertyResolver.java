package net.ripe.db.whois.common.support;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WhoisPropertyResolver {
    private final Properties properties;

    public WhoisPropertyResolver() throws IOException {
        InputStream inputStream = null;

        try {
            inputStream = new ClassPathResource("query.properties").getInputStream();
            properties = new Properties();
            properties.load(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
