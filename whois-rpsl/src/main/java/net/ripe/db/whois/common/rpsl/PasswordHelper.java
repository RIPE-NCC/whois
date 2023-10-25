package net.ripe.db.whois.common.rpsl;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordHelper {
    private static final Pattern MD5_PATTERN = Pattern.compile("(?i)MD5-PW ((\\$1\\$.{1,8})\\$.{22})");

    private static final String BASIC_AUTH_NAME_PASSWORD_SEPARATOR = ":";
    public static boolean authenticateMd5Passwords(final String authValue, final String... passwords) {
        return authenticateMd5Passwords(authValue, Arrays.asList(passwords));
    }

    public static boolean authenticateMd5Passwords(final String authValue, final Iterable<String> passwords) {
        final Matcher matcher = MD5_PATTERN.matcher(authValue);
        if (matcher.matches()) {
            final String known = matcher.group(1);
            final String salt = matcher.group(2);

            for (String password : passwords) {
                final String offered = Md5Crypt.md5Crypt(password.getBytes(), salt);
                if (known.equals(offered)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static KeyValue extractBasicAuthNameAndPassword(final String basicAuthCredentials){
        if (basicAuthCredentials == null || !basicAuthCredentials.contains(BASIC_AUTH_NAME_PASSWORD_SEPARATOR)) {
            return null;
        }
        final String[] basicAuthSplitCredentials = basicAuthCredentials.split(BASIC_AUTH_NAME_PASSWORD_SEPARATOR, 2);
        return new DefaultKeyValue(basicAuthSplitCredentials[0], basicAuthSplitCredentials[1]);

    }

    public static String hashMd5Password(final String cleantextPassword) {
        return Md5Crypt.md5Crypt(cleantextPassword.getBytes());
    }
}
