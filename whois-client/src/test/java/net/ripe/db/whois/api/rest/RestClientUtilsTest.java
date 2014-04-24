package net.ripe.db.whois.api.rest;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class RestClientUtilsTest {

    @Test
    public void encode_curly_braces() {
        assertThat(encode(""), contains(""));
        assertThat(encode("123"), contains("123"));
        assertThat(encode("{}"), contains("%7B%7D"));
        assertThat(encode("{"), contains("%7B"));
        assertThat(encode("{%7D"), contains("%7B%7D"));
    }

    private Iterable<String> encode(String... input) {
        return Iterables.transform(Arrays.asList(input), RestClientUtils.CURLY_BRACES_ENCODING_FUNCTION);
    }
}
