package net.ripe.db.whois.api.rest.mapper;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.regex.Pattern;

public class ValidXmlAdapter extends XmlAdapter<String, String> {

    private static final Pattern INVALID_CHARS_XML1_0 = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]");

    @Override
    public String unmarshal(final String v) throws Exception {
        return v;
    }

    @Override
    public String marshal(final String v) throws Exception {
        return removeNonvalidXmlCharacters(v);
    }

    private static String removeNonvalidXmlCharacters(final String str) {
        return INVALID_CHARS_XML1_0.matcher(str).replaceAll("");
    }
}
