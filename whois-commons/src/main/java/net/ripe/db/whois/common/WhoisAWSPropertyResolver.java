package net.ripe.db.whois.common;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClient;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersByPathResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Configuration
@Profile(WhoisProfile.AWS_DEPLOYED)
@PropertySource(value = "classpath:version.properties", ignoreResourceNotFound = true)
public class WhoisAWSPropertyResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisAWSPropertyResolver.class);

    static {
        try {
            final String hostname = InetAddress.getLocalHost().getHostName();
            System.setProperty("host.name", hostname);
            LOGGER.info("Instance hostname is {}", hostname);
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException("Could not determine hostname", uhe);
        }
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties(){
        LOGGER.info("AWS profile : using ssm parameter store");

        Properties properties = new Properties();
        properties.putAll( getParametersByEnvAndApp("/whois/"));

        PropertySourcesPlaceholderConfigurer propertySourceConfig = new PropertySourcesPlaceholderConfigurer();
        propertySourceConfig.setProperties(properties);

        ensureDirectoriesExist(properties);

        return propertySourceConfig;
    }

    private static void ensureDirectoriesExist(final Properties properties) {
        properties.forEach((k, v) -> {
            final String name = (String) k;
            if (name.startsWith("dir.")) {
                final String value = (String) v;
                LOGGER.info("Checking {} exists", value);
                File dir = new File(value);

                if (!dir.exists()) {
                    LOGGER.info("{} does not exist, creating", value);
                    try {
                        FileUtils.forceMkdir(dir);
                    } catch (IOException ioe) {
                        throw new IllegalStateException("Could not create output directory " + value, ioe);
                    }
                }
            }
        });
    }

    public static Map<String, Object> getParametersByEnvAndApp(final String path) {
        final AWSSimpleSystemsManagement awsSimpleSystemsManagement = AWSSimpleSystemsManagementClient.builder()
                .withRegion(System.getenv("AWS_REGION") ).build();

        final GetParametersByPathRequest getParametersByPathRequest = new GetParametersByPathRequest()
                .withPath(path)
                .withWithDecryption(true)
                .withRecursive(true);

        String token = null;
        final Map<String, Object> params = new HashMap<>();

        do {
            getParametersByPathRequest.setNextToken(token);
            final GetParametersByPathResult parameterResult = awsSimpleSystemsManagement.getParametersByPath(getParametersByPathRequest);
            token = parameterResult.getNextToken();

            params.putAll(addParamsToMap(parameterResult.getParameters()));

        } while (token != null);

        return params;
    }

    private static Map<String,String> addParamsToMap(List<Parameter> parameters) {
        return parameters.stream()
                .collect(Collectors.toMap(
                        parameter -> parameter.getName().substring(parameter.getName().lastIndexOf("/") + 1),
                        parameter -> parameter.getValue().trim()
                        )
                );
    }
}