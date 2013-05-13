package net.ripe.db.whois.common.support;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;

public class FileHelper {
    public static String fileToString(final String fileName) {
        try {
            return FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource(fileName).getInputStream()));
        } catch (IOException e) {
            throw new NestableRuntimeException(e);
        }
    }
}
