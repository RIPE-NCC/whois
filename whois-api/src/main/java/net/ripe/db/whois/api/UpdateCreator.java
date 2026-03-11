package net.ripe.db.whois.api;

import net.ripe.db.whois.common.PunycodeConversion;
import net.ripe.db.whois.common.RpslObjectCharacterConversion;
import net.ripe.db.whois.common.rpsl.RpslObject;
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

        final String punycodeConversion = PunycodeConversion.convert(rpslObject);
        final RpslObject convertedRpsl = RpslObjectCharacterConversion.paragraphConversion(punycodeConversion);

        final Update update = new Update(paragraph, operation, deleteReasons, convertedRpsl);
        if (!punycodeConversion.equals(rpslObject)) {
            updateContext.addMessage(update, UpdateMessages.valueChangedDueToPunycodeConversion());
        }

        return update;
    }
}
