package net.ripe.db.whois.query.planner;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.domain.MessageObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class SyntaxFilterFunctionTest {
    private final SyntaxFilterFunction validSyntaxFilterFunction = new SyntaxFilterFunction(true);
    private final SyntaxFilterFunction novalidSyntaxFilterFunction = new SyntaxFilterFunction(false);

    @Test
    public void validSyntax_valid_flag() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        final Iterable<? extends ResponseObject> result = validSyntaxFilterFunction.apply(object);

        final ResponseObject responseObject = Iterables.find(result, new Predicate<ResponseObject>() {
            @Override
            public boolean apply(final ResponseObject input) {
                return input instanceof RpslObject;
            }
        });
        assertThat(responseObject, is(not(nullValue())));
    }

    @Test
    public void invalidSyntax_valid_flag() {
        final RpslObject object = RpslObject.parse("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   wrong@address.net\n" +
                "nic-hdl: tst-ripe\n" +
                "mnt-by:  TST-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST");

        final Iterable<? extends ResponseObject> result = validSyntaxFilterFunction.apply(object);

        assertThat(Iterables.size(result), is(1));
        assertThat(Iterables.getFirst(result, null), is((ResponseObject)new MessageObject(QueryMessages.invalidSyntax("tst-ripe"))));
    }

    @Test
    public void validSyntax_novalid_flag() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        final Iterable<? extends ResponseObject> result = novalidSyntaxFilterFunction.apply(object);

        assertThat(Iterables.size(result), is(1));
        assertThat(Iterables.getFirst(result, null), is((ResponseObject)new MessageObject(QueryMessages.validSyntax("TST-MNT"))));
    }

    @Test
    public void invalidSyntax_novalid_flag() {
        final RpslObject object = RpslObject.parse("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   wrong@address.net\n" +
                "nic-hdl: tst-ripe\n" +
                "mnt-by:  TST-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST");

        final Iterable<? extends ResponseObject> result = novalidSyntaxFilterFunction.apply(object);

        final ResponseObject responseObject = Iterables.find(result, new Predicate<ResponseObject>() {
            @Override
            public boolean apply(final ResponseObject input) {
                return input instanceof RpslObject;
            }
        });
        assertThat(responseObject, is(not(nullValue())));
    }
}
