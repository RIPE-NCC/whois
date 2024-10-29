package net.ripe.db.whois.common.x509;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class ClientAuthCertificateValidator {

    private final DateTimeProvider dateTimeProvider;

    private final boolean clientAuthEnabled;

    private final RpslObjectDao rpslObjectDao;

    public ClientAuthCertificateValidator(@Qualifier("jdbcRpslObjectSlaveDao") final RpslObjectDao rpslObjectDao,
                                          final DateTimeProvider dateTimeProvider,
                                          @Value("${port.client.auth:-1}") final int clientAuthPort) {
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.clientAuthEnabled = clientAuthPort >= 0;
    }

    public boolean existValidCertificate(final List<RpslAttribute> authAttributes, final List<X509CertificateWrapper> certificates){
        if (CollectionUtils.isEmpty(certificates) || !this.isEnabled()) {
            return false;
        }

        for (RpslAttribute authAttribute : authAttributes) {
            CIString key = authAttribute.getCleanValue();
            if (key.startsWith("x509")) {
                final RpslObject object = rpslObjectDao.getByKey(ObjectType.KEY_CERT, key);
                final X509CertificateWrapper x509CertificateWrapper = X509CertificateWrapper.parse(object);

                if (x509CertificateWrapper.isExpired(dateTimeProvider)) {
                    continue;
                }

                if (x509CertificateWrapper.isNotYetValid(dateTimeProvider)) {
                    continue;
                }

                boolean isAuthCertificateAuthenticated = certificates.stream()
                        .map(X509CertificateWrapper::getCertificate)
                        .anyMatch(userCertificate -> userCertificate.equals(x509CertificateWrapper.getCertificate()));
                if (isAuthCertificateAuthenticated){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEnabled(){
        return clientAuthEnabled;
    }
}
