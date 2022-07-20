package net.ripe.db.whois.api.util;


import net.ripe.db.whois.common.rpsl.RpslObject;

public class TestUtil {
    public static final String AS102_STRING = "" +
            "aut-num:        AS102\n" +
            "as-name:        End-User-2\n" +
            "descr:          description\n" +
            "admin-c:        TP1-TEST\n" +
            "tech-c:         TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n" +
            "created:        2017-05-16T11:18:05Z\n" +
            "last-modified:  2017-05-16T11:18:05Z\n";

    public static final RpslObject AS102 = RpslObject.parse(AS102_STRING);

    public static final String TEST_PERSON_STRING = "" +
            "person:         Test Person\n" +
            "address:        Singel 258\n" +
            "phone:          +31 6 12345678\n" +
            "nic-hdl:        TP1-TEST\n" +
            "mnt-by:         OWNER-MNT\n" +
            "source:         TEST\n";

    public static final RpslObject TEST_PERSON = RpslObject.parse(TEST_PERSON_STRING);


}
