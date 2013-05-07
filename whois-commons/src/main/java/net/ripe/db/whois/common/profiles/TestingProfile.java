package net.ripe.db.whois.common.profiles;

import org.springframework.context.annotation.Profile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Profile(value = {WhoisProfile.TEST, WhoisProfile.ENDTOEND})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestingProfile {
}
