package net.ripe.db.whois.common.rpsl;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum RpslCharset {

    LATIN1(StandardCharsets.ISO_8859_1, "LATIN1, LATIN-1"),

    UTF8(StandardCharsets.UTF_8, "UTF8, UTF-8");

    final Charset charset;
    final List<String> commonNames;

    RpslCharset(final Charset charset, final String commonNames){
        this.charset = charset;
        final Splitter SAME_VALUES_SPLITTER = Splitter.on(",");
        this.commonNames = SAME_VALUES_SPLITTER.splitToList(commonNames);
    }

    public Charset getCharset() {
        return charset;
    }

    public List<String> getCommonNames() {
        return commonNames;
    }
}
