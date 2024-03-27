package net.ripe.db.whois.api.rest.mapper;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Arrays;
import java.util.List;

public class ValidListXmlAdapter extends XmlAdapter<String, List<String>> {

    @Override
    public List<String> unmarshal(String value) {
        String[] parts = value.split("\\n{2,}");
        return Arrays.asList(parts);
    }

    @Override
    public String marshal(List<String> value) {
        return String.join("\n\n", value);
    }
}
