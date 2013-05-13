package net.ripe.db.whois.common.support;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;

public class ByteArrayContains extends BaseMatcher<byte[]> {

    private byte[] needle;

    public ByteArrayContains(byte[] needle) {
        this.needle = needle;
    }

    public static final int indexOf(byte[] haystack, byte[] needle) {
        return indexOf(haystack, needle, 0);
    }

    /* Searches through haystack, returns index of first occurence of needle */
    public static final int indexOf(byte[] haystack, byte[] needle, int frompos) {
        int lengthDiff = haystack.length - frompos - needle.length;
        if (lengthDiff < 0) {
            return -1;
        }

        outer:
        for (int i = frompos; i <= lengthDiff; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }

        return -1;
    }

    /* Searches through haystack, returns index of first occurence of needle,
     * while ignoring ignoredChars from haystack */
    public static final int indexOfIgnoring(byte[] haystack, byte[] needle, int frompos, byte[] ignoredChars) {
        int lastMatch = haystack.length - needle.length;
        if (lastMatch < frompos) {
            return -1;
        }

        int hayind = frompos;
        int haymatch = frompos;
        int needind = 0;

        outer:
        while (hayind <= lastMatch) {
            // check if ignored char
            for (byte ignoredChar : ignoredChars) {
                if (haystack[hayind] == ignoredChar) {
                    hayind++;
                    continue outer;
                }
            }

            // check if the next char in needle matches haystack
            if (haystack[hayind] == needle[needind]) {
                needind++;
                // check if reached end if needle
                if (needind >= needle.length) {
                    return haymatch;
                }
            } else {    // reset counters
                needind = 0;
                haymatch = hayind + 1;
            }
            hayind++;
        }

        return -1;
    }

    @Override
    public boolean matches(Object haystack) {
        return indexOf((byte[]) haystack, needle) >= 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("contains(" + Arrays.toString(needle) + ")");
    }
}
