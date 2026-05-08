package net.ripe.db.whois.api.httpserver.jmx;

import com.google.common.net.InetAddresses;
import net.ripe.db.whois.common.jmx.JmxBase;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import net.ripe.db.whois.query.acl.AccessControlListManager;

@Component
@DeployedProfile
@ManagedResource(objectName = JmxBase.OBJECT_NAME_BASE + "PersonalAccountingManager", description = "PersonalAccountingManager operations")
public class PersonalAccountingManagerJmx extends JmxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonalAccountingManagerJmx.class);

    private final AccessControlListManager accessControlListManager;

    @Autowired
    public PersonalAccountingManagerJmx(final AccessControlListManager accessControlListManager) {
        super(LOGGER);
        this.accessControlListManager = accessControlListManager;
    }

    @ManagedOperation(description = "Get number of personal object can be queried by Ip")
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "ipAddress", description = "Ip address"),
    })
    public int getPersonalCountByIp(final String ipAddress) {
        try {
            return accessControlListManager.getPersonalObjects(accessControlListManager.getAccountingIdentifier(InetAddresses.forString(ipAddress), null));
        } catch (Exception e) {
            LOGGER.error("Failed to get personal object count by IP", e);
            throw e;
        }
    }

    @ManagedOperation(description = "Get number of personal object can be queried by SSO")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "username", description = "sso account for user"),
    })
    public int getPersonalCountBySSO(final String username) {
        try {
            return accessControlListManager.getPersonalObjectsBySSO(username);
        } catch (Exception e) {
            LOGGER.error("Failed to get personal object count by SSO", e);
            throw e;
        }
    }
}
