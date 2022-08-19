package net.ripe.db.whois.api.unit.rest.checks;

import net.ripe.db.whois.api.rest.checks.ServicesInputChecker;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ServicesInputCheckerTest {

    private final ServicesInputChecker servicesInputChecker = new ServicesInputChecker();

    @Test
    void test_ripe_nserver_incorrect_prefixes_then_error(){
        final Paragraph paragraph = new Paragraph(" ");
        final RpslObject submittedObject = new RpslObject(List.of(new RpslAttribute(AttributeType.DOMAIN,
                        CIString.ciString("e.0.0.0.a.1.ip6.arpa")), new RpslAttribute(AttributeType.DESCR,
                        CIString.ciString("Reverse delegation for 1a00:fb8::/23")),
                new RpslAttribute(AttributeType.NSERVER, CIString.ciString("ns.ripe.net"))));

        List<Update> updates = List.of(new Update(paragraph, Operation.UNSPECIFIED, List.of(" "), submittedObject));
        try {
            servicesInputChecker.checkNserverCorrectPrefixes(updates);
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Is not allowed to use that prefix with ns.ripe.net name server", e.getMessage());
        }
    }

}
