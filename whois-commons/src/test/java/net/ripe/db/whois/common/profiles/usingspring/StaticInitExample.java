package net.ripe.db.whois.common.profiles.usingspring;


import com.google.common.collect.Lists;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.AbstractEnvironment;

import java.util.List;

public class StaticInitExample {

    // If we're trying to use spring profiles to initialize *everything* then we need to interrogate "spring.profiles.active"
    // system prop at jvm startup.
    // The whois variant switch should only be set via a single system property!
    // There are plain java classes that currently rely on static initialisation to configure their values for either
    // RIPE or APNIC configs (e.g. rpsl syntax classes)
    static List<String> profiles = Lists.newArrayList();
    static {
        String[] values  = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "").toLowerCase().split(",");
        for (String value : values) {
            profiles.add(value.trim());
        }
    }

    public static boolean isAPNIC() {
//        return profiles.contains("apnic") or
        return profiles.contains(ApnicProfile.class.getAnnotation(Profile.class).value()[0]);
    }

    public static boolean isRIPE() {
//        return profiles.contains("ripe") or
        return profiles.contains(RipeProfile.class.getAnnotation(Profile.class).value()[0]);
    }


}
