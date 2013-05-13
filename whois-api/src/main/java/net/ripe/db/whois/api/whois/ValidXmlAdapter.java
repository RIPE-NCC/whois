package net.ripe.db.whois.api.whois;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.regex.Pattern;

public class ValidXmlAdapter extends XmlAdapter<String, String> {

    private static final Pattern INVALID_CHARS_XML1_0 = Pattern.compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\u10000-\\u10FFFF]");

    @Override
    public String unmarshal(String v) throws Exception {
        return v;
    }

    @Override
    public String marshal(String v) throws Exception {
        return removeNonvalidXmlCharacters(v);
    }

    private static String removeNonvalidXmlCharacters(String str) {
        return INVALID_CHARS_XML1_0.matcher(str).replaceAll("");
    }
}
