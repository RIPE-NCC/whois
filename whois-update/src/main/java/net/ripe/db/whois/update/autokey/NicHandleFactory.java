package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.autokey.dao.NicHandleRepository;
import net.ripe.db.whois.update.dao.CountryCodeRepository;
import net.ripe.db.whois.update.domain.NicHandle;
import net.ripe.db.whois.update.domain.NicHandleParseException;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NicHandleFactory extends AbstractAutoKeyFactory<NicHandle> {

    private final NicHandleRepository nicHandleRepository;
    private final CountryCodeRepository countryCodeRepository;

    @Autowired
    public NicHandleFactory(final NicHandleRepository nicHandleRepository, final CountryCodeRepository countryCodeRepository) {
        super(nicHandleRepository);

        this.nicHandleRepository = nicHandleRepository;
        this.countryCodeRepository = countryCodeRepository;
    }

    @Override
    public AttributeType getAttributeType() {
        return AttributeType.NIC_HDL;
    }

    @Override
    public NicHandle generate(final String keyPlaceHolder, final RpslObject object) {
        return generateForName(keyPlaceHolder, object.getTypeAttribute().getCleanValue().toString());
    }

    @Override
    public NicHandle claim(final String key) throws ClaimException {
        try {
            final NicHandle nicHandle = NicHandle.parse(key, getSource(), countryCodeRepository.getCountryCodes());
            if (!nicHandleRepository.claimSpecified(nicHandle)) {
                throw new ClaimException(UpdateMessages.nicHandleNotAvailable(nicHandle.toString()));
            }

            return nicHandle;
        } catch (NicHandleParseException e) {
            throw new ClaimException(ValidationMessages.syntaxError(key));
        }
    }
}
