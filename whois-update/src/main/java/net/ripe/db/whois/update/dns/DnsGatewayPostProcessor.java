package net.ripe.db.whois.update.dns;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.Map;

@DeployedProfile
@Component
public class DnsGatewayPostProcessor implements BeanDefinitionRegistryPostProcessor,ResourceLoaderAware,EnvironmentAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DnsGatewayPostProcessor.class);

    private Environment environment;
    private ResourceLoader resourceLoader;

    @Override
    public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) throws BeansException {
        // skip
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (readSystemProperties().containsKey("zonemaster")) {
            LOGGER.info("Found zonemaster property, using zonemaster");
            beanFactory.getBeanDefinition("dnsGatewayImpl").setPrimary(false);
            beanFactory.getBeanDefinition("zonemasterDnsGateway").setPrimary(true);
        } else {
            LOGGER.info("Defaulting to dnscheck");
            beanFactory.getBeanDefinition("dnsGatewayImpl").setPrimary(true);
            beanFactory.getBeanDefinition("zonemasterDnsGateway").setPrimary(false);
        }
    }

    private Map<String, Object> readSystemProperties() {
        final Map<String, Object> properties = Maps.newHashMap();
        if (environment instanceof ConfigurableEnvironment) {
            for (PropertySource propertySource : ((ConfigurableEnvironment)environment).getPropertySources()) {
                if (propertySource instanceof MapPropertySource) {
                    properties.putAll(((MapPropertySource) propertySource).getSource());
                }
            }
        }
        return properties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
