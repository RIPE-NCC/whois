package net.ripe.db.whois.api;

import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.Latin1ConversionResult;
import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;

import java.util.List;

public class UpdateCreator {

    public static Update createUpdate(final Paragraph paragraph,
                                      final Operation operation,
                                      final List<String> deleteReasons,
                                      final String rpslObject,
                                      final UpdateContext updateContext) {

        final String punycodeResult = PunycodeConversion.convert(rpslObject);

        final Latin1ConversionResult latin1ConversionResult = Latin1Conversion.convert(punycodeResult);

        final Update update = new Update(paragraph, operation, deleteReasons, latin1ConversionResult.getRpslObject());

        if (!punycodeResult.equals(rpslObject)) {
            updateContext.addMessage(update, UpdateMessages.valueChangedDueToPunycodeConversion());
        }

        if (latin1ConversionResult.isGlobalSubstitution()) {
            updateContext.addMessage(update, UpdateMessages.valueChangedDueToLatin1Conversion());
        }

        latin1ConversionResult.getSubstitutedAttributes().forEach(attr -> updateContext.addMessage(update, attr, UpdateMessages.valueChangedDueToLatin1Conversion(attr.getKey())));

        return update;
    }
}
