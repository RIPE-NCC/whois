package net.ripe.db.whois.common.profiles;

public class WhoisVariant {
    protected static final String WHOIS_VARIANT_SYS_ENV = "whois.variant";

    public static boolean isAPNIC() {
        return System.getProperty(WHOIS_VARIANT_SYS_ENV, "").equals(Type.APNIC.getValue());
    }

    public enum Type {
        APNIC("apnic"),
        NONE("");

        private final String value;

        Type(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return value;
        }
    }

    public static Type getWhoIsVariant() {
        Type ret = Type.NONE;
        if (System.getProperty(WHOIS_VARIANT_SYS_ENV, "").equalsIgnoreCase(Type.APNIC.getValue())) {
            ret = Type.APNIC;
        }
        return ret;
    }

    public static void setWhoIsVariant(Type type) {
        System.setProperty(WHOIS_VARIANT_SYS_ENV, type.getValue());
    }

}
