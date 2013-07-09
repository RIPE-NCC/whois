package net.ripe.db.whois.common.profiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.io.IOException;

public class WhoisVariantContextFilter implements TypeFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisVariantContextFilter.class);
    private static final Class annotationClass = WhoisVariantContext.class;

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        boolean match = hasDefaultContextAnnotations(metadataReader);

        if (match && metadataReader.getAnnotationMetadata().hasAnnotation(annotationClass.getCanonicalName())) {
            WhoisVariant.Type variant = WhoisVariant.getWhoIsVariant();
            LOGGER.info("WhoisVariant=" + variant);
            LOGGER.info("(metadataReader.getClassMetadata().getEnclosingClassName()=" + metadataReader.getClassMetadata().getClassName());
            Class clazz;
            try {
                clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
            } catch (ClassNotFoundException cnfe) {
                throw new IOException("Class could not be loaded by [" + getClass().getName() + "]", cnfe);
            }
            WhoisVariantContext whoisVariantContextAnnotation = (WhoisVariantContext) clazz.getAnnotation(annotationClass);
            if (whoisVariantContextAnnotation != null) {
                // See if this class should be included in the spring context based on whois variant setting and annotation params
                match = includeWhenMatch(metadataReader, match, whoisVariantContextAnnotation, variant);

                // See if this class should be excluded in the spring context based on whois variant setting and annotation params
                if (match) {
                    match = excludeWhenMatch(metadataReader, match, whoisVariantContextAnnotation, variant);
                }
            }
        }
        return match;
    }

    private boolean includeWhenMatch(MetadataReader metadataReader, boolean match, WhoisVariantContext whoisVariantContextAnnotation,  WhoisVariant.Type variant) {
        WhoisVariant.Type includeWhen = whoisVariantContextAnnotation.includeWhen();
        LOGGER.info("includeWhen [" + includeWhen.getValue() + "]");
        match = false;
        if (variant == includeWhen) {
            LOGGER.info("Including bean from spring context based on annotation profile: " + metadataReader.getClassMetadata().getClassName() + ":" + variant);
            match = true;
        }
        return match;
    }

    private boolean excludeWhenMatch(MetadataReader metadataReader, boolean match, WhoisVariantContext whoisVariantContextAnnotation, WhoisVariant.Type variant) {
        WhoisVariant.Type excludeWhen = whoisVariantContextAnnotation.excludeWhen();
        LOGGER.info("excludeWhen [" + excludeWhen.getValue() + "]");
        if (variant == excludeWhen) {
            LOGGER.info("Excluding bean from spring context based on annotation profile: " + metadataReader.getClassMetadata().getClassName() + ":" + variant);
            match = false;
        }
        return match;
    }


    private boolean hasDefaultContextAnnotations(MetadataReader metadataReader) {
        boolean ret = false;
        if (metadataReader.getAnnotationMetadata().hasAnnotation(Component.class.getCanonicalName()) ||
                metadataReader.getAnnotationMetadata().hasAnnotation(Repository.class.getCanonicalName()) ||
                metadataReader.getAnnotationMetadata().hasAnnotation(Service.class.getCanonicalName()) ||
                metadataReader.getAnnotationMetadata().hasAnnotation(Controller.class.getCanonicalName())) {
            ret = true;
        }
        return ret;
    }

}
