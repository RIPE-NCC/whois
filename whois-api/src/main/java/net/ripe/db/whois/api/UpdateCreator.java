package net.ripe.db.whois.api;

import net.ripe.db.whois.common.Utf8Conversion;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;

import java.util.List;

public class UpdateCreator {

    public static Update createUpdate(final Paragraph paragraph,
                                      final Operation operation,
                                      final List<String> deleteReasons,
                                      final String rpslObject) {

        final RpslObject convertedObject = Utf8Conversion.convert(rpslObject);
        return new Update(paragraph, operation, deleteReasons, convertedObject);

    }
}
