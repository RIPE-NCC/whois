package net.ripe.db.whois.compare.rest;

import net.ripe.db.whois.compare.common.QueryReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

public class RestQueryReader extends QueryReader {

    public enum RestResponseType {
        ALL, XML, JSON, TEXT
    }

    private final RestResponseType restResponseType;

    public RestQueryReader(final String inputFileLocation, final RestResponseType restResponseType){
        super (new ClassPathResource(inputFileLocation));
        this.restResponseType = restResponseType;
    }

    @Override
    protected String getQuery(final String line) {
        if (StringUtils.isNotBlank(line)
                && !line.startsWith("#")
                && isAllowed(line, restResponseType)) {
            return line;
        } else {
            return "";
        }
    }

    private boolean isAllowed(final String line, final RestResponseType restResponseType){
        switch (restResponseType) {
            case JSON:
                return line.contains(".json");
            case TEXT:
                return line.contains(".txt");
            case XML:
                return !line.contains(".json") && !line.contains(".txt");
            case ALL:
            default:
                return true;
        }
    }
}

