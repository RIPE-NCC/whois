package net.ripe.db.nrtm4.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class ByteArrayUtil {

    public static String byteArrayToHexString(final byte[] bytes) {
        final StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (final byte b : bytes) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static ByteArrayOutputStream asByteArrayOutputStream(final String str) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream(str.length());
        bos.write(str.getBytes(StandardCharsets.UTF_8));
        bos.close();
        return bos;
    }
}
