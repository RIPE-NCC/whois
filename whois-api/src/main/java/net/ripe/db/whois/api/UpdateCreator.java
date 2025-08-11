package net.ripe.db.whois.api;

import net.ripe.db.whois.common.Latin1Conversion;
import net.ripe.db.whois.common.Latin1ConversionResult;
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

        final Latin1ConversionResult conversionResult = Latin1Conversion.convert(rpslObject);
        final Update update = new Update(paragraph, operation, deleteReasons, conversionResult.getRpslObject());

        if (conversionResult.isGlobalSubstitution()) {
            updateContext.addMessage(update, UpdateMessages.valueChangedDueToLatin1Conversion());
        }

        conversionResult.getSubstitutedAttributes().forEach(attr -> updateContext.addMessage(update, attr, UpdateMessages.valueChangedDueToLatin1Conversion(attr.getKey())));

        return update;
    }
}
